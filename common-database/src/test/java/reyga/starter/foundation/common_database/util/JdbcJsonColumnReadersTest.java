package reyga.starter.foundation.common_database.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcJsonColumnReadersTest {

    @Test
    void should_ReadTextAtLimit_When_DriverReturnsJsonText() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        String document = "{\"nested\":{\"items\":[1,2]}}";
        when(resultSet.getString("metadata")).thenReturn(document);

        String result = JdbcJsonColumnReaders.text(document.length()).read(resultSet, "metadata");

        assertEquals(document, result);
        verify(resultSet, never()).next();
        verify(resultSet, never()).close();
    }

    @Test
    void should_PreserveSqlNull_When_StandardReadersReturnNoValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        assertNull(JdbcJsonColumnReaders.text().read(resultSet, "metadata"));
        assertNull(JdbcJsonColumnReaders.characterStream().read(resultSet, "metadata"));
        assertNull(JdbcJsonColumnReaders.utf8BinaryStream().read(resultSet, "metadata"));
    }

    @Test
    void should_ReadClobText_When_CharacterStreamIsSelected() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        String document = "{\"city\":\"東京\",\"nested\":{\"values\":[1,2]}}";
        SerialClob clob = new SerialClob(document.toCharArray());
        when(resultSet.getCharacterStream("metadata")).thenReturn(clob.getCharacterStream());

        String result = JdbcJsonColumnReaders.characterStream(document.length()).read(resultSet, "metadata");

        assertEquals(document, result);
    }

    @Test
    void should_CloseCharacterStream_When_ReadingSucceeds() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        Reader reader = mock(Reader.class);
        when(reader.read(any(char[].class))).thenReturn(-1);
        when(resultSet.getCharacterStream("metadata")).thenReturn(reader);

        String result = JdbcJsonColumnReaders.characterStream().read(resultSet, "metadata");

        assertEquals("", result);
        verify(reader).close();
        verify(resultSet, never()).close();
    }

    @Test
    void should_ReadUtf8BlobText_When_BinaryStreamIsSelected() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        String document = "{\"city\":\"東京\",\"emoji\":\"😀\"}";
        SerialBlob blob = new SerialBlob(document.getBytes(StandardCharsets.UTF_8));
        when(resultSet.getBinaryStream("metadata")).thenReturn(blob.getBinaryStream());

        String result = JdbcJsonColumnReaders.utf8BinaryStream(document.length()).read(resultSet, "metadata");

        assertEquals(document, result);
    }

    @Test
    void should_RejectOversizedText_When_DocumentExceedsLimit() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn("{}");

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcJsonColumnReaders.text(1).read(resultSet, "metadata"));

        assertEquals("22001", exception.getSQLState());
        assertTrue(exception.getMessage().contains("metadata"));
    }

    @Test
    void should_CloseCharacterStream_When_DocumentExceedsLimit() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ClosingStringReader stream = new ClosingStringReader("{\"nested\":{}}");
        when(resultSet.getCharacterStream("metadata")).thenReturn(stream);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcJsonColumnReaders.characterStream(2).read(resultSet, "metadata"));

        assertEquals("22001", exception.getSQLState());
        assertTrue(stream.closed);
    }

    @Test
    void should_CloseBinaryStream_When_DocumentExceedsLimit() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ClosingInputStream stream = new ClosingInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(resultSet.getBinaryStream("metadata")).thenReturn(stream);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcJsonColumnReaders.utf8BinaryStream(1).read(resultSet, "metadata"));

        assertEquals("22001", exception.getSQLState());
        assertTrue(stream.closed);
    }

    @Test
    void should_RejectMalformedUtf8AndCloseStream_When_BinaryContentIsInvalid() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ClosingInputStream stream = new ClosingInputStream(new byte[]{(byte) 0xC3, (byte) 0x28});
        when(resultSet.getBinaryStream("metadata")).thenReturn(stream);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcJsonColumnReaders.utf8BinaryStream().read(resultSet, "metadata"));

        assertEquals("22000", exception.getSQLState());
        assertTrue(exception.getCause() instanceof IOException);
        assertTrue(stream.closed);
    }

    @Test
    void should_CloseBinaryStream_When_ReadingSucceeds() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ClosingInputStream stream = new ClosingInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(resultSet.getBinaryStream("metadata")).thenReturn(stream);

        assertEquals("{}", JdbcJsonColumnReaders.utf8BinaryStream().read(resultSet, "metadata"));
        assertTrue(stream.closed);
    }

    @Test
    void should_PreserveIoCauseAndCloseStream_When_CharacterReadFails() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        Reader reader = mock(Reader.class);
        IOException failure = new IOException("Read failed");
        when(reader.read(any(char[].class))).thenThrow(failure);
        when(resultSet.getCharacterStream("metadata")).thenReturn(reader);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcJsonColumnReaders.characterStream().read(resultSet, "metadata"));

        assertEquals("22000", exception.getSQLState());
        assertSame(failure, exception.getCause());
        verify(reader).close();
    }

    @Test
    void should_PreserveSqlCause_When_DriverDoesNotSupportConversion() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("Unsupported column conversion", "0A000");
        when(resultSet.getCharacterStream("metadata")).thenThrow(failure);

        SQLException exception = assertThrows(SQLException.class,
                () -> JdbcJsonColumnReaders.characterStream().read(resultSet, "metadata"));

        assertSame(failure, exception);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void should_RejectInvalidLimit_When_ReaderIsConfigured(int limit) {
        assertThrows(IllegalArgumentException.class, () -> JdbcJsonColumnReaders.text(limit));
        assertThrows(IllegalArgumentException.class, () -> JdbcJsonColumnReaders.characterStream(limit));
        assertThrows(IllegalArgumentException.class, () -> JdbcJsonColumnReaders.utf8BinaryStream(limit));
    }

    @Test
    void should_RejectInvalidColumnBeforeReading_When_ReaderArgumentsAreInvalid() {
        ResultSet resultSet = mock(ResultSet.class);

        assertThrows(NullPointerException.class, () -> JdbcJsonColumnReaders.text().read(null, "metadata"));
        assertThrows(NullPointerException.class, () -> JdbcJsonColumnReaders.characterStream().read(resultSet, null));
        assertThrows(IllegalArgumentException.class, () -> JdbcJsonColumnReaders.utf8BinaryStream().read(resultSet, " "));
        verifyNoInteractions(resultSet);
    }

    private static final class ClosingStringReader extends StringReader {
        private boolean closed;

        private ClosingStringReader(String value) {
            super(value);
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }

    private static final class ClosingInputStream extends ByteArrayInputStream {
        private boolean closed;

        private ClosingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
