package cz.jeme.programu.stolujemeapi.sql;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public interface ResultWrapper extends WrapperWrapper<ResultWrapper, ResultSet> {
    boolean next() throws SQLException;

    boolean wasNull() throws SQLException;

    // Delegate values

    @Nullable
    String getString(final int columnIndex) throws SQLException;

    @Nullable
    String getString() throws SQLException;

    boolean getBoolean(final int columnIndex) throws SQLException;

    boolean getBoolean() throws SQLException;

    byte getByte(final int columnIndex) throws SQLException;

    byte getByte() throws SQLException;

    short getShort(final int columnIndex) throws SQLException;

    short getShort() throws SQLException;

    int getInt(final int columnIndex) throws SQLException;

    int getInt() throws SQLException;

    long getLong(final int columnIndex) throws SQLException;

    long getLong() throws SQLException;

    float getFloat(final int columnIndex) throws SQLException;

    float getFloat() throws SQLException;

    double getDouble(final int columnIndex) throws SQLException;

    double getDouble() throws SQLException;

    byte @Nullable [] getBytes(final int columnIndex) throws SQLException;

    byte @Nullable [] getBytes() throws SQLException;

    @Nullable
    Date getDate(final int columnIndex) throws SQLException;

    @Nullable
    Date getDate() throws SQLException;

    @Nullable
    Time getTime(final int columnIndex) throws SQLException;

    @Nullable
    Time getTime() throws SQLException;

    @Nullable
    Timestamp getTimestamp(final int columnIndex) throws SQLException;

    @Nullable
    Timestamp getTimestamp() throws SQLException;

    @Nullable
    InputStream getAsciiStream(final int columnIndex) throws SQLException;

    @Nullable
    InputStream getAsciiStream() throws SQLException;

    @Nullable
    InputStream getBinaryStream(final int columnIndex) throws SQLException;

    @Nullable
    InputStream getBinaryStream() throws SQLException;

    @Nullable
    Object getObject(final int columnIndex) throws SQLException;

    @Nullable
    Object getObject() throws SQLException;

    @Nullable
    Reader getCharacterStream(final int columnIndex) throws SQLException;

    @Nullable
    Reader getCharacterStream() throws SQLException;

    @Nullable
    BigDecimal getBigDecimal(final int columnIndex) throws SQLException;

    @Nullable
    BigDecimal getBigDecimal() throws SQLException;

    @Nullable
    Object getObject(final int columnIndex, final @NotNull Map<String, Class<?>> map) throws SQLException;

    @Nullable
    Object getObject(final @NotNull Map<String, Class<?>> map) throws SQLException;

    @Nullable
    Ref getRef(final int columnIndex) throws SQLException;

    @Nullable
    Ref getRef() throws SQLException;

    @Nullable
    Blob getBlob(final int columnIndex) throws SQLException;

    @Nullable
    Blob getBlob() throws SQLException;

    @Nullable
    Clob getClob(final int columnIndex) throws SQLException;

    @Nullable
    Clob getClob() throws SQLException;

    @Nullable
    Array getArray(final int columnIndex) throws SQLException;

    @Nullable
    Array getArray() throws SQLException;

    @Nullable
    Date getDate(final int columnIndex, final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    Date getDate(final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    Time getTime(final int columnIndex, final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    Time getTime(final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    Timestamp getTimestamp(final int columnIndex, final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    Timestamp getTimestamp(final @NotNull Calendar calendar) throws SQLException;

    @Nullable
    URL getURL(final int columnIndex) throws SQLException;

    @Nullable
    URL getURL() throws SQLException;

    @Nullable
    RowId getRowId(final int columnIndex) throws SQLException;

    @Nullable
    RowId getRowId() throws SQLException;

    @Nullable
    NClob getNClob(final int columnIndex) throws SQLException;

    @Nullable
    NClob getNClob() throws SQLException;

    @Nullable
    SQLXML getSQLXML(final int columnIndex) throws SQLException;

    @Nullable
    SQLXML getSQLXML() throws SQLException;

    @Nullable
    String getNString(final int columnIndex) throws SQLException;

    @Nullable
    String getNString() throws SQLException;

    @Nullable
    Reader getNCharacterStream(final int columnIndex) throws SQLException;

    @Nullable
    Reader getNCharacterStream() throws SQLException;

    <T> @Nullable T getObject(final int columnIndex, final @NotNull Class<T> type) throws SQLException;

    <T> @Nullable T getObject(final @NotNull Class<T> type) throws SQLException;

    // Stolujeme nullables

    @Nullable
    Boolean getNullBoolean(final int columnIndex) throws SQLException;

    @Nullable
    Boolean getNullBoolean() throws SQLException;

    @Nullable
    Byte getNullByte(final int columnIndex) throws SQLException;

    @Nullable
    Byte getNullByte() throws SQLException;

    @Nullable
    Short getNullShort(final int columnIndex) throws SQLException;

    @Nullable
    Short getNullShort() throws SQLException;

    @Nullable
    Integer getNullInteger(final int columnIndex) throws SQLException;

    @Nullable
    Integer getNullInteger() throws SQLException;

    @Nullable
    Long getNullLong(final int columnIndex) throws SQLException;

    @Nullable
    Long getNullLong() throws SQLException;

    @Nullable
    Float getNullFloat(final int columnIndex) throws SQLException;

    @Nullable
    Float getNullFloat() throws SQLException;

    @Nullable
    Double getNullDouble(final int columnIndex) throws SQLException;

    @Nullable
    Double getNullDouble() throws SQLException;

    // Stolujeme time

    @Nullable
    LocalDate getLocalDate(final int columnIndex) throws SQLException;

    @Nullable
    LocalDate getLocalDate() throws SQLException;

    @Nullable
    LocalTime getLocalTime(final int columnIndex) throws SQLException;

    @Nullable
    LocalTime getLocalTime() throws SQLException;

    @Nullable
    LocalDateTime getLocalDateTime(final int columnIndex) throws SQLException;

    @Nullable
    LocalDateTime getLocalDateTime() throws SQLException;

    // Stolujeme values

    @Nullable
    UUID getUUID(final int columnIndex) throws SQLException;

    @Nullable
    UUID getUUID() throws SQLException;

    @Nullable
    Canteen getCanteen(final int columnIndex) throws SQLException;

    @Nullable
    Canteen getCanteen() throws SQLException;

    @Nullable
    Meal.Course getCourse(final int columnIndex) throws SQLException;

    @Nullable
    Meal.Course getCourse() throws SQLException;

    @Nullable
    File getFile(final int columnIndex) throws SQLException;

    @Nullable
    File getFile() throws SQLException;
}