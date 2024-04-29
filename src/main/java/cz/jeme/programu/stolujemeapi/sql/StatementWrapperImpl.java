package cz.jeme.programu.stolujemeapi.sql;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import org.jetbrains.annotations.ApiStatus;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Internal
final class StatementWrapperImpl implements StatementWrapper {
    private final @NotNull ResultWrapper resultWrapper = new ResultWrapperImpl();
    private @Nullable PreparedStatement statement;
    private final @NotNull Set<Integer> occupied = new HashSet<>();
    private int next = 1;

    @Override
    public @NotNull StatementWrapper wrap(final @NotNull PreparedStatement statement) throws SQLException {
        if (wrapped()) clear();
        this.statement = statement;
        return this;
    }

    @Override
    public @NotNull PreparedStatement unwrap() throws SQLException {
        if (statement == null)
            throw new IllegalStateException("Trying to unwrap whilst not wrapping anything!");
        final PreparedStatement pointer = statement;
        clear();
        return pointer;
    }

    @Override
    public @NotNull StatementWrapper clear() throws SQLException {
        statement().close();
        statement = null;
        occupied.clear();
        next = 1;
        return this;
    }

    private int index() {
        while (occupied.contains(next)) next++;
        next++;
        return next - 1;
    }

    private void occupy(final int parameterIndex) {
        occupied.add(parameterIndex);
    }

    @Override
    public boolean wrapped() {
        return statement != null;
    }

    private @NotNull PreparedStatement statement() {
        if (statement == null)
            throw new IllegalStateException("Trying to update wrapper whilst not wrapping anything!");
        return statement;
    }

    // Delegate values

    @Override
    public @NotNull StatementWrapper setNull(final int parameterIndex, final int sqlType) throws SQLException {
        statement().setNull(parameterIndex, sqlType);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNull(final int sqlType) throws SQLException {
        return setNull(index(), sqlType);
    }

    @Override
    public @NotNull StatementWrapper setBoolean(final int parameterIndex, final boolean b) throws SQLException {
        statement().setBoolean(parameterIndex, b);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBoolean(final boolean b) throws SQLException {
        return setBoolean(index(), b);
    }

    @Override
    public @NotNull StatementWrapper setByte(final int parameterIndex, final byte b) throws SQLException {
        statement().setByte(parameterIndex, b);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setByte(final byte b) throws SQLException {
        return setByte(index(), b);
    }

    @Override
    public @NotNull StatementWrapper setShort(final int parameterIndex, final short s) throws SQLException {
        statement().setShort(parameterIndex, s);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setShort(final short s) throws SQLException {
        return setShort(index(), s);
    }

    @Override
    public @NotNull StatementWrapper setInt(final int parameterIndex, final int i) throws SQLException {
        statement().setInt(parameterIndex, i);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setInt(final int i) throws SQLException {
        return setInt(index(), i);
    }

    @Override
    public @NotNull StatementWrapper setLong(final int parameterIndex, final long l) throws SQLException {
        statement().setLong(parameterIndex, l);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setLong(final long l) throws SQLException {
        return setLong(index(), l);
    }

    @Override
    public @NotNull StatementWrapper setFloat(final int parameterIndex, final float f) throws SQLException {
        statement().setFloat(parameterIndex, f);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setFloat(final float f) throws SQLException {
        return setFloat(index(), f);
    }

    @Override
    public @NotNull StatementWrapper setDouble(final int parameterIndex, final double d) throws SQLException {
        statement().setDouble(parameterIndex, d);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setDouble(final double d) throws SQLException {
        return setDouble(index(), d);
    }

    @Override
    public @NotNull StatementWrapper setBigDecimal(final int parameterIndex, final @NotNull BigDecimal bigDecimal) throws SQLException {
        statement().setBigDecimal(parameterIndex, bigDecimal);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBigDecimal(final @NotNull BigDecimal bigDecimal) throws SQLException {
        return setBigDecimal(index(), bigDecimal);
    }

    @Override
    public @NotNull StatementWrapper setString(final int parameterIndex, final @NotNull String string) throws SQLException {
        statement().setString(parameterIndex, string);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setString(final @NotNull String string) throws SQLException {
        return setString(index(), string);
    }

    @Override
    public @NotNull StatementWrapper setBytes(final int parameterIndex, final byte @NotNull [] bytes) throws SQLException {
        statement().setBytes(parameterIndex, bytes);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBytes(final byte @NotNull [] bytes) throws SQLException {
        return setBytes(index(), bytes);
    }

    @Override
    public @NotNull StatementWrapper setDate(final int parameterIndex, final @NotNull Date date) throws SQLException {
        statement().setDate(parameterIndex, date);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setDate(final @NotNull Date date) throws SQLException {
        return setDate(index(), date);
    }

    @Override
    public @NotNull StatementWrapper setTime(final int parameterIndex, final @NotNull Time time) throws SQLException {
        statement().setTime(parameterIndex, time);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setTime(final @NotNull Time time) throws SQLException {
        return setTime(index(), time);
    }

    @Override
    public @NotNull StatementWrapper setTimestamp(final int parameterIndex, final @NotNull Timestamp timestamp) throws SQLException {
        statement().setTimestamp(parameterIndex, timestamp);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setTimestamp(final @NotNull Timestamp timestamp) throws SQLException {
        return setTimestamp(index(), timestamp);
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream, final int length) throws SQLException {
        statement().setAsciiStream(parameterIndex, inputStream, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final @NotNull InputStream inputStream, final int length) throws SQLException {
        return setAsciiStream(index(), inputStream, length);
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream, final int length) throws SQLException {
        statement().setBinaryStream(parameterIndex, inputStream, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final @NotNull InputStream inputStream, final int length) throws SQLException {
        return setBinaryStream(index(), inputStream, length);
    }

    @Override
    public @NotNull StatementWrapper setObject(final int parameterIndex, final @NotNull Object object, final int targetSqlType) throws SQLException {
        statement().setObject(parameterIndex, object, targetSqlType);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setObject(final @NotNull Object object, final int targetSqlType) throws SQLException {
        return setObject(index(), object, targetSqlType);
    }

    @Override
    public @NotNull StatementWrapper setObject(final int parameterIndex, final @NotNull Object object) throws SQLException {
        statement().setObject(parameterIndex, object);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setObject(final @NotNull Object object) throws SQLException {
        return setObject(index(), object);
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader, final int length) throws SQLException {
        statement().setCharacterStream(parameterIndex, reader, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final @NotNull Reader reader, final int length) throws SQLException {
        return setCharacterStream(index(), reader, length);
    }

    @Override
    public @NotNull StatementWrapper setRef(final int parameterIndex, final @NotNull Ref ref) throws SQLException {
        statement().setRef(parameterIndex, ref);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setRef(final @NotNull Ref ref) throws SQLException {
        return setRef(index(), ref);
    }

    @Override
    public @NotNull StatementWrapper setBlob(final int parameterIndex, final @NotNull Blob blob) throws SQLException {
        statement().setBlob(parameterIndex, blob);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBlob(final @NotNull Blob blob) throws SQLException {
        return setBlob(index(), blob);
    }

    @Override
    public @NotNull StatementWrapper setClob(final int parameterIndex, final @NotNull Clob clob) throws SQLException {
        statement().setClob(parameterIndex, clob);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setClob(final @NotNull Clob clob) throws SQLException {
        return setClob(index(), clob);
    }

    @Override
    public @NotNull StatementWrapper setArray(final int parameterIndex, final @NotNull Array array) throws SQLException {
        statement().setArray(parameterIndex, array);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setArray(final @NotNull Array array) throws SQLException {
        return setArray(index(), array);
    }

    @Override
    public @NotNull StatementWrapper setDate(final int parameterIndex, final @NotNull Date date, final @NotNull Calendar calendar) throws SQLException {
        statement().setDate(parameterIndex, date, calendar);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setDate(final @NotNull Date date, final @NotNull Calendar calendar) throws SQLException {
        return setDate(index(), date, calendar);
    }

    @Override
    public @NotNull StatementWrapper setTime(final int parameterIndex, final @NotNull Time time, final @NotNull Calendar calendar) throws SQLException {
        statement().setTime(parameterIndex, time, calendar);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setTime(final @NotNull Time time, final @NotNull Calendar calendar) throws SQLException {
        return setTime(index(), time, calendar);
    }

    @Override
    public @NotNull StatementWrapper setTimestamp(final int parameterIndex, final @NotNull Timestamp timestamp, final @NotNull Calendar calendar) throws SQLException {
        statement().setTimestamp(parameterIndex, timestamp, calendar);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setTimestamp(final @NotNull Timestamp timestamp, final @NotNull Calendar calendar) throws SQLException {
        return setTimestamp(index(), timestamp, calendar);
    }

    @Override
    public @NotNull StatementWrapper setNull(final int parameterIndex, final int sqlType, final @NotNull String typeName) throws SQLException {
        statement().setNull(parameterIndex, sqlType, typeName);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNull(final int sqlType, final @NotNull String typeName) throws SQLException {
        return setNull(index(), sqlType, typeName);
    }

    @Override
    public @NotNull StatementWrapper setURL(final int parameterIndex, final @NotNull URL url) throws SQLException {
        statement().setURL(parameterIndex, url);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setURL(final @NotNull URL url) throws SQLException {
        return setURL(index(), url);
    }

    @Override
    public @NotNull StatementWrapper setRowId(final int parameterIndex, final @NotNull RowId rowId) throws SQLException {
        statement().setRowId(parameterIndex, rowId);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setRowId(final @NotNull RowId rowId) throws SQLException {
        return setRowId(index(), rowId);
    }

    @Override
    public @NotNull StatementWrapper setNString(final int parameterIndex, final @NotNull String value) throws SQLException {
        statement().setNString(parameterIndex, value);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNString(final @NotNull String value) throws SQLException {
        return setNString(index(), value);
    }

    @Override
    public @NotNull StatementWrapper setNCharacterStream(final int parameterIndex, final @NotNull Reader value, final long length) throws SQLException {
        statement().setNCharacterStream(parameterIndex, value, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNCharacterStream(final @NotNull Reader value, final long length) throws SQLException {
        return setNCharacterStream(index(), value, length);
    }

    @Override
    public @NotNull StatementWrapper setNClob(final int parameterIndex, final @NotNull NClob value) throws SQLException {
        statement().setNClob(parameterIndex, value);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNClob(final @NotNull NClob value) throws SQLException {
        return setNClob(index(), value);
    }

    @Override
    public @NotNull StatementWrapper setClob(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException {
        statement().setClob(parameterIndex, reader, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setClob(final @NotNull Reader reader, final long length) throws SQLException {
        return setClob(index(), reader, length);
    }

    @Override
    public @NotNull StatementWrapper setBlob(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException {
        statement().setBlob(parameterIndex, inputStream, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBlob(final @NotNull InputStream inputStream, final long length) throws SQLException {
        return setBlob(index(), inputStream, length);
    }

    @Override
    public @NotNull StatementWrapper setNClob(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException {
        statement().setNClob(parameterIndex, reader, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNClob(final @NotNull Reader reader, final long length) throws SQLException {
        return setNClob(index(), reader, length);
    }

    @Override
    public @NotNull StatementWrapper setSQLXML(final int parameterIndex, final @NotNull SQLXML xmlObject) throws SQLException {
        statement().setSQLXML(parameterIndex, xmlObject);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setSQLXML(final @NotNull SQLXML xmlObject) throws SQLException {
        return setSQLXML(index(), xmlObject);
    }

    @Override
    public @NotNull StatementWrapper setObject(final int parameterIndex, final @NotNull Object object, final int targetSqlType, final int scaleOrLength) throws SQLException {
        statement().setObject(parameterIndex, object, targetSqlType, scaleOrLength);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setObject(final @NotNull Object object, final int targetSqlType, final int scaleOrLength) throws SQLException {
        return setObject(index(), object, targetSqlType, scaleOrLength);
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException {
        statement().setAsciiStream(parameterIndex, inputStream, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final @NotNull InputStream inputStream, final long length) throws SQLException {
        return setAsciiStream(index(), inputStream, length);
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream, final long length) throws SQLException {
        statement().setBinaryStream(parameterIndex, inputStream, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final @NotNull InputStream inputStream, final long length) throws SQLException {
        return setBinaryStream(index(), inputStream, length);
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader, final long length) throws SQLException {
        statement().setCharacterStream(parameterIndex, reader, length);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final @NotNull Reader reader, final long length) throws SQLException {
        return setCharacterStream(index(), reader, length);
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException {
        statement().setAsciiStream(parameterIndex, inputStream);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setAsciiStream(final @NotNull InputStream inputStream) throws SQLException {
        return setAsciiStream(index(), inputStream);
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException {
        statement().setBinaryStream(parameterIndex, inputStream);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBinaryStream(final @NotNull InputStream inputStream) throws SQLException {
        return setBinaryStream(index(), inputStream);
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final int parameterIndex, final @NotNull Reader reader) throws SQLException {
        statement().setCharacterStream(parameterIndex, reader);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setCharacterStream(final @NotNull Reader reader) throws SQLException {
        return setCharacterStream(index(), reader);
    }

    @Override
    public @NotNull StatementWrapper setNCharacterStream(final int parameterIndex, final @NotNull Reader reader) throws SQLException {
        statement().setNCharacterStream(parameterIndex, reader);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNCharacterStream(final @NotNull Reader reader) throws SQLException {
        return setNCharacterStream(index(), reader);
    }

    @Override
    public @NotNull StatementWrapper setClob(final int parameterIndex, final @NotNull Reader reader) throws SQLException {
        statement().setClob(parameterIndex, reader);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setClob(final @NotNull Reader reader) throws SQLException {
        return setClob(index(), reader);
    }

    @Override
    public @NotNull StatementWrapper setBlob(final int parameterIndex, final @NotNull InputStream inputStream) throws SQLException {
        statement().setBlob(parameterIndex, inputStream);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setBlob(final @NotNull InputStream inputStream) throws SQLException {
        return setBlob(index(), inputStream);
    }

    @Override
    public @NotNull StatementWrapper setNClob(final int parameterIndex, final @NotNull Reader reader) throws SQLException {
        statement().setNClob(parameterIndex, reader);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setNClob(final @NotNull Reader reader) throws SQLException {
        return setNClob(index(), reader);
    }

    @Override
    public @NotNull StatementWrapper setObject(final int parameterIndex,
                                               final @NotNull Object object,
                                               final @NotNull SQLType targetSqlType,
                                               final int scaleOrLength) throws SQLException {
        statement().setObject(parameterIndex, object, targetSqlType, scaleOrLength);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setObject(final @NotNull Object object,
                                               final @NotNull SQLType targetSqlType,
                                               final int scaleOrLength) throws SQLException {
        return setObject(index(), object, targetSqlType, scaleOrLength);
    }

    @Override
    public @NotNull StatementWrapper setObject(final int parameterIndex,
                                               final @NotNull Object object,
                                               final @NotNull SQLType targetSqlType) throws SQLException {
        statement().setObject(parameterIndex, object, targetSqlType);
        occupy(parameterIndex);
        return this;
    }

    @Override
    public @NotNull StatementWrapper setObject(final @NotNull Object object,
                                               final @NotNull SQLType targetSqlType) throws SQLException {
        return setObject(index(), object, targetSqlType);
    }

    // Delegate executions

    @Override
    public boolean execute() throws SQLException {
        return statement().execute();
    }

    @Override
    public @NotNull ResultWrapper executeQuery() throws SQLException {
        return resultWrapper.wrap(statement().executeQuery());
    }

    @Override
    public long @NotNull [] executeLargeBatch() throws SQLException {
        return statement().executeLargeBatch();
    }

    @Override
    public int executeUpdate() throws SQLException {
        return statement().executeUpdate();
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return statement().executeLargeUpdate();
    }

    // Stolujeme executions

    @Override
    public @NotNull ResultWrapper executeGenerate() throws SQLException {
        execute();
        return resultWrapper.wrap(statement().getGeneratedKeys());
    }

    // Stolujeme nullables

    @Override
    public @NotNull StatementWrapper setDefaultNull(final int parameterIndex) throws SQLException {
        return setNull(parameterIndex, Types.NULL);
    }

    @Override
    public @NotNull StatementWrapper setDefaultNull() throws SQLException {
        return setDefaultNull(index());
    }

    @FunctionalInterface
    private interface NullComputer<V> {
        @NotNull
        StatementWrapper apply(final int parameterIndex, final @NotNull V value) throws SQLException;
    }

    private <T> @NotNull StatementWrapper nullCompute(final int parameterIndex,
                                                      final @Nullable T value,
                                                      final @NotNull NullComputer<T> computer) throws SQLException {
        return value == null
                ? setDefaultNull(parameterIndex)
                : computer.apply(parameterIndex, value);
    }

    @Override
    public @NotNull StatementWrapper setNullBoolean(final int parameterIndex, final @Nullable Boolean b) throws SQLException {
        return nullCompute(parameterIndex, b, this::setBoolean);
    }

    @Override
    public @NotNull StatementWrapper setNullBoolean(final @Nullable Boolean b) throws SQLException {
        return setNullBoolean(index(), b);
    }

    @Override
    public @NotNull StatementWrapper setNullByte(final int parameterIndex, final @Nullable Byte b) throws SQLException {
        return nullCompute(parameterIndex, b, this::setByte);
    }

    @Override
    public @NotNull StatementWrapper setNullByte(final @Nullable Byte b) throws SQLException {
        return setNullByte(index(), b);
    }

    @Override
    public @NotNull StatementWrapper setNullShort(final int parameterIndex, final @Nullable Short s) throws SQLException {
        return nullCompute(parameterIndex, s, this::setShort);
    }

    @Override
    public @NotNull StatementWrapper setNullShort(final @Nullable Short s) throws SQLException {
        return setNullShort(index(), s);
    }

    @Override
    public @NotNull StatementWrapper setNullInteger(final int parameterIndex, final @Nullable Integer i) throws SQLException {
        return nullCompute(parameterIndex, i, this::setInt);
    }

    @Override
    public @NotNull StatementWrapper setNullInteger(final @Nullable Integer i) throws SQLException {
        return setNullInteger(index(), i);
    }

    @Override
    public @NotNull StatementWrapper setNullLong(final int parameterIndex, final @Nullable Long l) throws SQLException {
        return nullCompute(parameterIndex, l, this::setLong);
    }

    @Override
    public @NotNull StatementWrapper setNullLong(final @Nullable Long l) throws SQLException {
        return setNullLong(index(), l);
    }

    @Override
    public @NotNull StatementWrapper setNullFloat(final int parameterIndex, final @Nullable Float f) throws SQLException {
        return nullCompute(parameterIndex, f, this::setFloat);
    }

    @Override
    public @NotNull StatementWrapper setNullFloat(final @Nullable Float f) throws SQLException {
        return setNullFloat(index(), f);
    }

    @Override
    public @NotNull StatementWrapper setNullDouble(final int parameterIndex, final @Nullable Double d) throws SQLException {
        return nullCompute(parameterIndex, d, this::setDouble);
    }

    @Override
    public @NotNull StatementWrapper setNullDouble(final @Nullable Double d) throws SQLException {
        return setNullDouble(index(), d);
    }

    @Override
    public @NotNull StatementWrapper setNullString(final int parameterIndex, final @Nullable String string) throws SQLException {
        return nullCompute(parameterIndex, string, this::setString);
    }

    @Override
    public @NotNull StatementWrapper setNullString(final @Nullable String string) throws SQLException {
        return setNullString(index(), string);
    }

    // Stolujeme time

    @Override
    public @NotNull StatementWrapper setLocalDate(final int parameterIndex, final @NotNull LocalDate localDate) throws SQLException {
        return setDate(parameterIndex, Date.valueOf(localDate));
    }

    @Override
    public @NotNull StatementWrapper setLocalDate(final @NotNull LocalDate localDate) throws SQLException {
        return setLocalDate(index(), localDate);
    }

    @Override
    public @NotNull StatementWrapper setLocalTime(final int parameterIndex, final @NotNull LocalTime localTime) throws SQLException {
        return setTime(parameterIndex, Time.valueOf(localTime));
    }

    @Override
    public @NotNull StatementWrapper setLocalTime(final @NotNull LocalTime localTime) throws SQLException {
        return setLocalTime(index(), localTime);
    }

    @Override
    public @NotNull StatementWrapper setLocalDateTime(final int parameterIndex, final @NotNull LocalDateTime localDateTime) throws SQLException {
        return setTimestamp(parameterIndex, Timestamp.valueOf(localDateTime));
    }

    @Override
    public @NotNull StatementWrapper setLocalDateTime(final @NotNull LocalDateTime localDateTime) throws SQLException {
        return setLocalDateTime(index(), localDateTime);
    }

    // Stolujeme values

    @Override
    public @NotNull StatementWrapper setUUID(final int parameterIndex, final @NotNull UUID uuid) throws SQLException {
        return setString(parameterIndex, uuid.toString());
    }

    @Override
    public @NotNull StatementWrapper setUUID(final @NotNull UUID uuid) throws SQLException {
        return setUUID(index(), uuid);
    }

    @Override
    public @NotNull StatementWrapper setCanteen(final int parameterIndex, final @NotNull Canteen canteen) throws SQLException {
        return setString(parameterIndex, canteen.name());
    }

    @Override
    public @NotNull StatementWrapper setCanteen(final @NotNull Canteen canteen) throws SQLException {
        return setCanteen(index(), canteen);
    }

    @Override
    public @NotNull StatementWrapper setCourse(final int parameterIndex, final @NotNull Meal.Course course) throws SQLException {
        return setString(parameterIndex, course.toString());
    }

    @Override
    public @NotNull StatementWrapper setCourse(final @NotNull Meal.Course course) throws SQLException {
        return setCourse(index(), course);
    }

    @Override
    public @NotNull StatementWrapper setFile(final int parameterIndex, final @NotNull File file) throws SQLException {
        return setString(parameterIndex, file.getAbsolutePath());
    }

    @Override
    public @NotNull StatementWrapper setFile(final @NotNull File file) throws SQLException {
        return setFile(index(), file);
    }
}