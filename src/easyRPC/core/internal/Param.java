package easyRPC.core.internal;

public final class Param {

    public enum ParamType
    {
        INT,
        FLOAT,
        DOUBLE,
        LONG,
        CHAR,
        SHORT,
        BOOL,
        STRING,
        INVALID
    }

    public final static int IntSize = 4;
    public final static int DoubleSize = 8;
    public final static int LongSize = 8;
    public final static int FloatSize = 4;
    public final static int CharSize = 2;
    public final static int ShortSize = 2;

}
