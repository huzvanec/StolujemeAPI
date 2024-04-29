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
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

@ApiStatus.Internal
final class ResultWrapperImpl implements ResultWrapper {
    private @Nullable ResultSet result;
    private final @NotNull Set<Integer> requested = new HashSet<>();
    private int next = 1;

    @Override
    public @NotNull ResultWrapper wrap(final @NotNull ResultSet result) throws SQLException {
        if (wrapped()) clear();
        this.result = result;
        return this;
    }

    @Override
    public @NotNull ResultSet unwrap() throws SQLException {
        if (result == null)
            throw new IllegalStateException("Trying to update wrapper whilst not wrapping anything!");
        final ResultSet pointer = result;
        clear();
        return pointer;
    }

    @Override
    public @NotNull ResultWrapper clear() throws SQLException {
        result().close();
        result = null;
        requested.clear();
        next = 1;
        return this;
    }

    private int index() {
        while (requested.contains(next)) next++;
        next++;
        return next - 1;
    }

    private void request(final int parameterIndex) {
        requested.add(parameterIndex);
    }

    @Override
    public boolean wrapped() {
        return result != null;
    }

    private @NotNull ResultSet result() {
        if (result == null)
            throw new IllegalStateException("Trying to set contents whilst not wrapping anything!");
        return result;
    }

    private <T> @Nullable T nullCoalesce(final int columnIndex, @NotNull final T supply) throws SQLException {
        return result().getObject(columnIndex) == null ? null : supply;
    }

    private <T, U> @Nullable U nullMap(final @Nullable T value, final @NotNull Function<T, U> mapper) {
        return value == null ? null : mapper.apply(value);
    }


    @Override
    public boolean next() throws SQLException {
        final boolean valid = result().next();
        next = 1;
        requested.clear();
        return valid;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return result().wasNull();
    }

    // Delegate values

    @Override
    public @Nullable String getString(final int columnIndex) throws SQLException {
        final String value = result().getString(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable String getString() throws SQLException {
        return getString(index());
    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        final boolean value = result().getBoolean(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public boolean getBoolean() throws SQLException {
        return getBoolean(index());
    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        final byte value = result().getByte(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public byte getByte() throws SQLException {
        return getByte(index());
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException {
        final short value = result().getShort(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public short getShort() throws SQLException {
        return getShort(index());
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException {
        final int value = result().getInt(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public int getInt() throws SQLException {
        return getInt(index());
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException {
        final long value = result().getLong(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public long getLong() throws SQLException {
        return getLong(index());
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        final float value = result().getFloat(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public float getFloat() throws SQLException {
        return getFloat(index());
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        final double value = result().getDouble(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public double getDouble() throws SQLException {
        return getDouble(index());
    }

    @Override
    public byte @Nullable [] getBytes(final int columnIndex) throws SQLException {
        final byte[] value = result().getBytes(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public byte @Nullable [] getBytes() throws SQLException {
        return getBytes(index());
    }

    @Override
    public @Nullable Date getDate(final int columnIndex) throws SQLException {
        final Date value = result().getDate(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Date getDate() throws SQLException {
        return getDate(index());
    }

    @Override
    public @Nullable Time getTime(final int columnIndex) throws SQLException {
        final Time value = result().getTime(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Time getTime() throws SQLException {
        return getTime(index());
    }

    @Override
    public @Nullable Timestamp getTimestamp(final int columnIndex) throws SQLException {
        final Timestamp value = result().getTimestamp(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Timestamp getTimestamp() throws SQLException {
        return getTimestamp(index());
    }

    @Override
    public @Nullable InputStream getAsciiStream(final int columnIndex) throws SQLException {
        final InputStream value = result().getAsciiStream(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable InputStream getAsciiStream() throws SQLException {
        return getAsciiStream(index());
    }

    @Override
    public @Nullable InputStream getBinaryStream(final int columnIndex) throws SQLException {
        final InputStream value = result().getBinaryStream(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable InputStream getBinaryStream() throws SQLException {
        return getBinaryStream(index());
    }

    @Override
    public @Nullable Object getObject(final int columnIndex) throws SQLException {
        final Object value = result().getObject(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Object getObject() throws SQLException {
        return getObject(index());
    }

    @Override
    public @Nullable Reader getCharacterStream(final int columnIndex) throws SQLException {
        final Reader value = result().getCharacterStream(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Reader getCharacterStream() throws SQLException {
        return getCharacterStream(index());
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        final BigDecimal value = result().getBigDecimal(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable BigDecimal getBigDecimal() throws SQLException {
        return getBigDecimal(index());
    }

    @Override
    public @Nullable Object getObject(final int columnIndex, final @NotNull Map<String, Class<?>> map) throws SQLException {
        final Object value = result().getObject(columnIndex, map);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Object getObject(final @NotNull Map<String, Class<?>> map) throws SQLException {
        return getObject(index(), map);
    }

    @Override
    public @Nullable Ref getRef(final int columnIndex) throws SQLException {
        final Ref value = result().getRef(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Ref getRef() throws SQLException {
        return getRef(index());
    }

    @Override
    public @Nullable Blob getBlob(final int columnIndex) throws SQLException {
        final Blob value = result().getBlob(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Blob getBlob() throws SQLException {
        return getBlob(index());
    }

    @Override
    public @Nullable Clob getClob(final int columnIndex) throws SQLException {
        final Clob value = result().getClob(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Clob getClob() throws SQLException {
        return getClob(index());
    }

    @Override
    public @Nullable Array getArray(final int columnIndex) throws SQLException {
        final Array value = result().getArray(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Array getArray() throws SQLException {
        return getArray(index());
    }

    @Override
    public @Nullable Date getDate(final int columnIndex, final @NotNull Calendar calendar) throws SQLException {
        final Date value = result().getDate(columnIndex, calendar);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Date getDate(final @NotNull Calendar calendar) throws SQLException {
        return getDate(index(), calendar);
    }

    @Override
    public @Nullable Time getTime(final int columnIndex, final @NotNull Calendar calendar) throws SQLException {
        final Time value = result().getTime(columnIndex, calendar);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Time getTime(final @NotNull Calendar calendar) throws SQLException {
        return getTime(index(), calendar);
    }

    @Override
    public @Nullable Timestamp getTimestamp(final int columnIndex, final @NotNull Calendar calendar) throws SQLException {
        final Timestamp value = result().getTimestamp(columnIndex, calendar);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Timestamp getTimestamp(final @NotNull Calendar calendar) throws SQLException {
        return getTimestamp(index(), calendar);
    }

    @Override
    public @Nullable URL getURL(final int columnIndex) throws SQLException {
        final URL value = result().getURL(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable URL getURL() throws SQLException {
        return getURL(index());
    }

    @Override
    public @Nullable RowId getRowId(final int columnIndex) throws SQLException {
        final RowId value = result().getRowId(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable RowId getRowId() throws SQLException {
        return getRowId(index());
    }

    @Override
    public @Nullable NClob getNClob(final int columnIndex) throws SQLException {
        final NClob value = result().getNClob(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable NClob getNClob() throws SQLException {
        return getNClob(index());
    }

    @Override
    public @Nullable SQLXML getSQLXML(final int columnIndex) throws SQLException {
        final SQLXML value = result().getSQLXML(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable SQLXML getSQLXML() throws SQLException {
        return getSQLXML(index());
    }

    @Override
    public @Nullable String getNString(final int columnIndex) throws SQLException {
        final String value = result().getNString(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable String getNString() throws SQLException {
        return getNString(index());
    }

    @Override
    public @Nullable Reader getNCharacterStream(final int columnIndex) throws SQLException {
        final Reader value = result().getNCharacterStream(columnIndex);
        request(columnIndex);
        return value;
    }

    @Override
    public @Nullable Reader getNCharacterStream() throws SQLException {
        return getNCharacterStream(index());
    }

    @Override
    public <T> @Nullable T getObject(final int columnIndex, final @NotNull Class<T> type) throws SQLException {
        final T value = result().getObject(columnIndex, type);
        request(columnIndex);
        return value;
    }

    @Override
    public <T> @Nullable T getObject(final @NotNull Class<T> type) throws SQLException {
        return getObject(index(), type);
    }

    // Stolujeme nullables

    @Override
    public @Nullable Boolean getNullBoolean(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getBoolean(columnIndex));
    }

    @Override
    public @Nullable Boolean getNullBoolean() throws SQLException {
        return getNullBoolean(index());
    }

    @Override
    public @Nullable Byte getNullByte(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getByte(columnIndex));
    }

    @Override
    public @Nullable Byte getNullByte() throws SQLException {
        return getNullByte(index());
    }

    @Override
    public @Nullable Short getNullShort(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getShort(columnIndex));
    }

    @Override
    public @Nullable Short getNullShort() throws SQLException {
        return getNullShort(index());
    }

    @Override
    public @Nullable Integer getNullInteger(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getInt(columnIndex));
    }

    @Override
    public @Nullable Integer getNullInteger() throws SQLException {
        return getNullInteger(index());
    }

    @Override
    public @Nullable Long getNullLong(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getLong(columnIndex));
    }

    @Override
    public @Nullable Long getNullLong() throws SQLException {
        return getNullLong(index());
    }

    @Override
    public @Nullable Float getNullFloat(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getFloat(columnIndex));
    }

    @Override
    public @Nullable Float getNullFloat() throws SQLException {
        return getNullFloat(index());
    }

    @Override
    public @Nullable Double getNullDouble(final int columnIndex) throws SQLException {
        return nullCoalesce(columnIndex, getDouble(columnIndex));
    }

    @Override
    public @Nullable Double getNullDouble() throws SQLException {
        return getNullDouble(index());
    }

    // Stolujeme time

    @Override
    public @Nullable LocalDate getLocalDate(final int columnIndex) throws SQLException {
        return nullMap(getDate(columnIndex), Date::toLocalDate);
    }

    @Override
    public @Nullable LocalDate getLocalDate() throws SQLException {
        return getLocalDate(index());
    }

    @Override
    public @Nullable LocalTime getLocalTime(final int columnIndex) throws SQLException {
        return nullMap(getTime(columnIndex), Time::toLocalTime);
    }

    @Override
    public @Nullable LocalTime getLocalTime() throws SQLException {
        return getLocalTime(index());
    }

    @Override
    public @Nullable LocalDateTime getLocalDateTime(final int columnIndex) throws SQLException {
        return nullMap(getTimestamp(columnIndex), Timestamp::toLocalDateTime);
    }

    @Override
    public @Nullable LocalDateTime getLocalDateTime() throws SQLException {
        return getLocalDateTime(index());
    }

    // Stolujeme values

    @Override
    public @Nullable UUID getUUID(final int columnIndex) throws SQLException {
        return nullMap(getString(columnIndex), UUID::fromString);
    }

    @Override
    public @Nullable UUID getUUID() throws SQLException {
        return getUUID(index());
    }

    @Override
    public @Nullable Canteen getCanteen(final int columnIndex) throws SQLException {
        return nullMap(getString(columnIndex), Canteen::fromName);
    }

    @Override
    public @Nullable Canteen getCanteen() throws SQLException {
        return getCanteen(index());
    }

    @Override
    public @Nullable Meal.Course getCourse(final int columnIndex) throws SQLException {
        return nullMap(getString(columnIndex), Meal.Course::valueOf);
    }

    @Override
    public @Nullable Meal.Course getCourse() throws SQLException {
        return getCourse(index());
    }

    @Override
    public @Nullable File getFile(final int columnIndex) throws SQLException {
        return nullMap(getString(columnIndex), File::new);
    }

    @Override
    public @Nullable File getFile() throws SQLException {
        return getFile(index());
    }
}
