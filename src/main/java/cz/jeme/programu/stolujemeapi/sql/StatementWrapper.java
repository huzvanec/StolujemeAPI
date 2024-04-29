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
import java.util.UUID;

public interface StatementWrapper extends WrapperWrapper<StatementWrapper, PreparedStatement> {
    static @NotNull StatementWrapper wrapper() {
        return new StatementWrapperImpl();
    }

    static @NotNull StatementWrapper wrapper(final @NotNull PreparedStatement statement) throws SQLException {
        return StatementWrapper.wrapper().wrap(statement);
    }

    // Delegate values

    @NotNull
    StatementWrapper setNull(final int parameterIndex, final int sqlType) throws SQLException;

    @NotNull
    StatementWrapper setNull(final int sqlType) throws SQLException;

    @NotNull
    StatementWrapper setBoolean(final int parameterIndex, final boolean b) throws SQLException;

    @NotNull
    StatementWrapper setBoolean(final boolean b) throws SQLException;

    @NotNull
    StatementWrapper setByte(final int parameterIndex, final byte b) throws SQLException;

    @NotNull
    StatementWrapper setByte(final byte b) throws SQLException;

    @NotNull
    StatementWrapper setShort(final int parameterIndex, final short s) throws SQLException;

    @NotNull
    StatementWrapper setShort(final short s) throws SQLException;

    @NotNull
    StatementWrapper setInt(final int parameterIndex, final int i) throws SQLException;

    @NotNull
    StatementWrapper setInt(final int i) throws SQLException;

    @NotNull
    StatementWrapper setLong(final int parameterIndex, final long l) throws SQLException;

    @NotNull
    StatementWrapper setLong(final long l) throws SQLException;

    @NotNull
    StatementWrapper setFloat(final int parameterIndex, final float f) throws SQLException;

    @NotNull
    StatementWrapper setFloat(final float f) throws SQLException;

    @NotNull
    StatementWrapper setDouble(final int parameterIndex, final double d) throws SQLException;

    @NotNull
    StatementWrapper setDouble(final double d) throws SQLException;

    @NotNull
    StatementWrapper setBigDecimal(final int parameterIndex, final @NotNull BigDecimal bigDecimal) throws SQLException;

    @NotNull
    StatementWrapper setBigDecimal(final @NotNull BigDecimal bigDecimal) throws SQLException;

    @NotNull
    StatementWrapper setString(final int parameterIndex, final @NotNull String string) throws SQLException;

    @NotNull
    StatementWrapper setString(final @NotNull String string) throws SQLException;

    @NotNull
    StatementWrapper setBytes(final int parameterIndex, final byte @NotNull [] bytes) throws SQLException;

    @NotNull
    StatementWrapper setBytes(final byte @NotNull [] bytes) throws SQLException;

    @NotNull
    StatementWrapper setDate(final int parameterIndex, final @NotNull Date date) throws SQLException;

    @NotNull
    StatementWrapper setDate(final @NotNull Date date) throws SQLException;

    @NotNull
    StatementWrapper setTime(final int parameterIndex, final @NotNull Time time) throws SQLException;

    @NotNull
    StatementWrapper setTime(final @NotNull Time time) throws SQLException;

    @NotNull
    StatementWrapper setTimestamp(final int parameterIndex, final @NotNull Timestamp timestamp) throws SQLException;

    @NotNull
    StatementWrapper setTimestamp(final @NotNull Timestamp timestamp) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream, int length) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final @NotNull InputStream inputStream, int length) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream, final int length) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final @NotNull InputStream inputStream, final int length) throws SQLException;

    @NotNull
    StatementWrapper setObject(final int parameterIndex, final @NotNull Object object, final int targetSqlType) throws SQLException;

    @NotNull
    StatementWrapper setObject(final @NotNull Object object, final int targetSqlType) throws SQLException;

    @NotNull
    StatementWrapper setObject(final int parameterIndex, final @NotNull Object object) throws SQLException;

    @NotNull
    StatementWrapper setObject(final @NotNull Object object) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader, final int length) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final @NotNull Reader reader, final int length) throws SQLException;

    @NotNull
    StatementWrapper setRef(final int parameterIndex, final @NotNull Ref ref) throws SQLException;

    @NotNull
    StatementWrapper setRef(final @NotNull Ref ref) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final int parameterIndex, final @NotNull Blob blob) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final @NotNull Blob blob) throws SQLException;

    @NotNull
    StatementWrapper setClob(final int parameterIndex, final @NotNull Clob clob) throws SQLException;

    @NotNull
    StatementWrapper setClob(final @NotNull Clob clob) throws SQLException;

    @NotNull
    StatementWrapper setArray(final int parameterIndex, final @NotNull Array array) throws SQLException;

    @NotNull
    StatementWrapper setArray(final @NotNull Array array) throws SQLException;

    @NotNull
    StatementWrapper setDate(final int parameterIndex, final @NotNull Date date, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setDate(final @NotNull Date date, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setTime(final int parameterIndex, final @NotNull Time time, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setTime(final @NotNull Time time, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setTimestamp(final int parameterIndex, final @NotNull Timestamp timestamp, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setTimestamp(final @NotNull Timestamp timestamp, final @NotNull Calendar calendar) throws SQLException;

    @NotNull
    StatementWrapper setNull(final int parameterIndex, final int sqlType, final @NotNull String typeName) throws SQLException;

    @NotNull
    StatementWrapper setNull(final int sqlType, final @NotNull String typeName) throws SQLException;

    @NotNull
    StatementWrapper setURL(final int parameterIndex, final @NotNull URL url) throws SQLException;

    @NotNull
    StatementWrapper setURL(final @NotNull URL url) throws SQLException;

    @NotNull
    StatementWrapper setRowId(final int parameterIndex, final @NotNull RowId rowId) throws SQLException;

    @NotNull
    StatementWrapper setRowId(final @NotNull RowId rowId) throws SQLException;

    @NotNull
    StatementWrapper setNString(final int parameterIndex, final @NotNull String value) throws SQLException;

    @NotNull
    StatementWrapper setNString(final @NotNull String value) throws SQLException;

    @NotNull
    StatementWrapper setNCharacterStream(final int parameterIndex, final @NotNull Reader value, long length) throws SQLException;

    @NotNull
    StatementWrapper setNCharacterStream(final @NotNull Reader value, long length) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final int parameterIndex, final @NotNull NClob value) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final @NotNull NClob value) throws SQLException;

    @NotNull
    StatementWrapper setClob(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setClob(final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setSQLXML(final int parameterIndex, final @NotNull SQLXML xmlObject) throws SQLException;

    @NotNull
    StatementWrapper setSQLXML(final @NotNull SQLXML xmlObject) throws SQLException;

    @NotNull
    StatementWrapper setObject(final int parameterIndex, final @NotNull Object object, final int targetSqlType, final int scaleOrLength) throws SQLException;

    @NotNull
    StatementWrapper setObject(final @NotNull Object object, final int targetSqlType, final int scaleOrLength) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final @NotNull InputStream inputStream, final long length) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final @NotNull Reader reader, final long length) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setAsciiStream(final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setBinaryStream(final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setCharacterStream(final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setNCharacterStream(final int parameterIndex, final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setNCharacterStream(final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setClob(final int parameterIndex, final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setClob(final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setBlob(final @NotNull InputStream inputStream) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final int parameterIndex, final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setNClob(final @NotNull Reader reader) throws SQLException;

    @NotNull
    StatementWrapper setObject(final int parameterIndex,
                               final @NotNull Object object,
                               final @NotNull SQLType targetSqlType,
                               final int scaleOrLength) throws SQLException;

    @NotNull
    StatementWrapper setObject(final @NotNull Object object,
                               final @NotNull SQLType targetSqlType,
                               final int scaleOrLength) throws SQLException;

    @NotNull
    StatementWrapper setObject(final int parameterIndex,
                               final @NotNull Object object,
                               final @NotNull SQLType targetSqlType) throws SQLException;

    @NotNull
    StatementWrapper setObject(final @NotNull Object object,
                               final @NotNull SQLType targetSqlType) throws SQLException;

    // Delegate executions

    boolean execute() throws SQLException;

    @NotNull
    ResultWrapper executeQuery() throws SQLException;

    long @NotNull [] executeLargeBatch() throws SQLException;

    int executeUpdate() throws SQLException;

    long executeLargeUpdate() throws SQLException;

    // Stolujeme executions

    @NotNull
    ResultWrapper executeGenerate() throws SQLException;

    // Stolujeme nullables

    @NotNull
    StatementWrapper setDefaultNull(final int parameterIndex) throws SQLException;

    @NotNull
    StatementWrapper setDefaultNull() throws SQLException;

    @NotNull
    StatementWrapper setNullBoolean(final int parameterIndex, final @Nullable Boolean b) throws SQLException;

    @NotNull
    StatementWrapper setNullBoolean(final @Nullable Boolean b) throws SQLException;

    @NotNull
    StatementWrapper setNullByte(final int parameterIndex, final @Nullable Byte b) throws SQLException;

    @NotNull
    StatementWrapper setNullByte(final @Nullable Byte b) throws SQLException;

    @NotNull
    StatementWrapper setNullShort(final int parameterIndex, final @Nullable Short s) throws SQLException;

    @NotNull
    StatementWrapper setNullShort(final @Nullable Short s) throws SQLException;

    @NotNull
    StatementWrapper setNullInteger(final int parameterIndex, final @Nullable Integer i) throws SQLException;

    @NotNull
    StatementWrapper setNullInteger(final @Nullable Integer i) throws SQLException;

    @NotNull
    StatementWrapper setNullLong(final int parameterIndex, final @Nullable Long l) throws SQLException;

    @NotNull
    StatementWrapper setNullLong(final @Nullable Long l) throws SQLException;

    @NotNull
    StatementWrapper setNullFloat(final int parameterIndex, final @Nullable Float f) throws SQLException;

    @NotNull
    StatementWrapper setNullFloat(final @Nullable Float f) throws SQLException;

    @NotNull
    StatementWrapper setNullDouble(final int parameterIndex, final @Nullable Double d) throws SQLException;

    @NotNull
    StatementWrapper setNullDouble(final @Nullable Double d) throws SQLException;

    @NotNull
    StatementWrapper setNullString(final int parameterIndex, final @Nullable String string) throws SQLException;

    @NotNull
    StatementWrapper setNullString(final @Nullable String string) throws SQLException;

    // Stolujeme time

    @NotNull
    StatementWrapper setLocalDate(final int parameterIndex, final @NotNull LocalDate localDate) throws SQLException;

    @NotNull
    StatementWrapper setLocalDate(final @NotNull LocalDate localDate) throws SQLException;

    @NotNull
    StatementWrapper setLocalTime(final int parameterIndex, final @NotNull LocalTime localTime) throws SQLException;

    @NotNull
    StatementWrapper setLocalTime(final @NotNull LocalTime localTime) throws SQLException;

    @NotNull
    StatementWrapper setLocalDateTime(final int parameterIndex, final @NotNull LocalDateTime localDateTime) throws SQLException;

    @NotNull
    StatementWrapper setLocalDateTime(final @NotNull LocalDateTime localDateTime) throws SQLException;

    // Stolujeme values

    @NotNull
    StatementWrapper setUUID(final int parameterIndex, final @NotNull UUID uuid) throws SQLException;

    @NotNull
    StatementWrapper setUUID(final @NotNull UUID uuid) throws SQLException;

    @NotNull
    StatementWrapper setCanteen(final int parameterIndex, final @NotNull Canteen canteen) throws SQLException;

    @NotNull
    StatementWrapper setCanteen(final @NotNull Canteen canteen) throws SQLException;

    @NotNull
    StatementWrapper setCourse(final int parameterIndex, final @NotNull Meal.Course course) throws SQLException;

    @NotNull
    StatementWrapper setCourse(final @NotNull Meal.Course course) throws SQLException;

    @NotNull
    StatementWrapper setFile(final int parameterIndex, final @NotNull File file) throws SQLException;

    @NotNull
    StatementWrapper setFile(final @NotNull File file) throws SQLException;
}