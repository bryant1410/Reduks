package com.beyondeye.reduks.logger.logformatter;

import com.beyondeye.reduks.logger.LogLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class LogFormatterPrinter {

    private static final String DEFAULT_TAG = "PRETTYLOGGER";

//    private static final int DEBUG = 3;
//    private static final int ERROR = 6;
//    private static final int ASSERT = 7;
//    private static final int INFO = 4;
//    private static final int VERBOSE = 2;
//    private static final int WARN = 5;

    /**
     * It is used for json pretty print
     */
    private static final int JSON_INDENT = 2;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 3;


    private static final String LINE_SEPARATOR_CHAR =System.getProperty("line.separator");
    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String HORIZONTAL_DOUBLE_LINE_STR = HORIZONTAL_DOUBLE_LINE+" ";
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * single level group blanks
     */
    private static final String SINGLE_GROUP_BLANKS = "  ";

    /**
     * tag is used for the Log, the name is a little different
     * in order to differentiate the logs easily with the filter
     */
    private String tag;

    /**
     * Localize single tag and method count and groupBlanks for each thread
     */
//    private final ThreadLocal<String> localTag = new ThreadLocal<String>();
    private int localMethodCount;
    private String groupBlanks = "";
    private String  groupCollapsed = "";

    /**
     * It is used to determine log settings such as method count, thread info visibility
     */
    private final LogFormatterSettings settings = new LogFormatterSettings();

    public LogFormatterPrinter() {
        init(DEFAULT_TAG);
    }

    /**
     * It is used to change the tag
     *
     * @param tag is the given string which will be used in LogFormatter
     */
    public LogFormatterSettings init(String tag) {
        if (tag == null) {
            throw new NullPointerException("tag may not be null");
        }
        if (tag.trim().length() == 0) {
            throw new IllegalStateException("tag may not be empty");
        }
        this.tag = tag;
        return settings;
    }

    public LogFormatterSettings getSettings() {
        return settings;
    }

    public void groupStart() {
        groupBlanks += SINGLE_GROUP_BLANKS;
    }
    
    public void groupEnd() {
        int newlength = groupBlanks.length() - SINGLE_GROUP_BLANKS.length();
        if (newlength >= 0) {
            groupBlanks= groupBlanks.substring(0, newlength);
        }
        //remove one level of collapse
        int newcollapsed = groupCollapsed.length() -1;
        if (newcollapsed >= 0) { //closed a collapsed group
            groupCollapsed = groupCollapsed.substring(0, newcollapsed);
            if(newcollapsed==0) { //closed all collapse level: empty buffer
                flushCollapsedBuffer();
            }
        }
    }


    public boolean isCollapsed() {
        return groupCollapsed.length()>0;
    }
    public void groupCollapsedStart() {
        groupStart();
        groupCollapsed +='c';
    }

//    public LogFormatterPrinter t(String localTag, int methodCount) {
//        if (localTag != null) {
//            this.localTag.set(localTag);
//        }
//        localMethodCount.set(methodCount);
//        return this;
//    }
    public LogFormatterPrinter t(int methodCount) {
        localMethodCount=methodCount;
        return this;
    }

//    @Override
//    public void d(String message, Object... args) {
//        log(DEBUG, null, message, args);
//    }


//    @Override
//    public void d(Object object) {
//        String message;
//        if (object.getClass().isArray()) {
//            message = Arrays.deepToString((Object[]) object);
//        } else {
//            message = object.toString();
//        }
//        log(DEBUG, null, message);
//    }

//    @Override
//    public void e(String message, Object... args) {
//        e(null, message, args);
//    }

//    @Override
//    public void e(Throwable throwable, String message, Object... args) {
//        log(ERROR, throwable, message, args);
//    }

//    @Override
//    public void w(String message, Object... args) {
//        log(WARN, null, message, args);
//    }

//    @Override
//    public void i(String message, Object... args) {
//        log(INFO, null, message, args);
//    }

//    @Override
//    public void v(String message, Object... args) {
//        log(VERBOSE, null, message, args);
//    }

//    @Override
//    public void wtf(String message, Object... args) {
//        log(ASSERT, null, message, args);
//    }
    private void d(String message) {
        log(LogLevel.DEBUG, null, message, null);
    }

    private void e(String message) {
        log(LogLevel.ERROR, null, message, null);
    }

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public void json(int logLevel,String objName,String json) {
        if (Helper.isEmpty(json)) {
            d("Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if(isCollapsed()) { //no json pretty printing if collapsed
                log(logLevel,null,jsonWithObjName(objName,json),null);
                return;
            }
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String formatted_json = jsonObject.toString(JSON_INDENT);
                log(logLevel,null,jsonWithObjName(objName,formatted_json),null);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String formatted_json = jsonArray.toString(JSON_INDENT);
                log(logLevel,null,jsonWithObjName(objName,formatted_json),null);
                return;
            }
            e("Invalid Json");
        } catch (JSONException e) {
            e("Invalid Json");
        }
    }
    //TODO use stringbuilder here?
    String jsonWithObjName(String objName, String json) {
        return objName+"="+json;
    }

    //TODO remove synchronized from here and put on reduks_logger printBuffer
    public synchronized void log(int loglevel, String tagSuffix, String message, Throwable throwable) {
        if (!settings.isLogEnabled()) {
            return;
        }
        if (isCollapsed()) log_collapsed(loglevel,tagSuffix,message,throwable);
        boolean isShowCallStack=settings.isShowCallStack();
        if (throwable != null && message != null) {
            message += " : " + Helper.getStackTraceString(throwable);
            isShowCallStack=true; //always enable call stack for exceptions
        }
        if (throwable != null && message == null) {
            message = Helper.getStackTraceString(throwable);
            isShowCallStack=true; //always enable call stack for exceptions
        }
//        if (message == null) {
//            message = "No message/exception is set";
//        }
//        if (Helper.isEmpty(message)) {
//            message = "Empty/NULL log message";
//        }

        int methodCount = getMethodCount();
        logTopBorder(loglevel, tagSuffix);
        boolean isShowThreadInfo = settings.isShowThreadInfo();
        logHeaderContent(loglevel, tagSuffix, methodCount, isShowThreadInfo, isShowCallStack);
        if (isShowCallStack && methodCount > 0) {
            logDivider(loglevel, tagSuffix);
        }
        logMessageBodyChunked(loglevel, tagSuffix, message);
    }

    private String msgBufferTagSuffix ="";
    private int    msgBufferLogLevel=-1;
    private StringBuilder msgbuffer=new StringBuilder();
    //TODO remove synchronized from here and put on reduks_logger printBuffer
    public synchronized void log_collapsed(int loglevel, String tagSuffix, String message, Throwable throwable) {
        boolean isShowCallStack=settings.isShowCallStack();
        boolean isFirstLineInCollapsedGroup=msgbuffer.length()==0;
        if (throwable != null && message != null) {
            message += " : " + Helper.getStackTraceString(throwable);
            isShowCallStack=true; //always enable call stack for exceptions
        }
        if (throwable != null && message == null) {
            message = Helper.getStackTraceString(throwable);
            isShowCallStack=true; //always enable call stack for exceptions
        }
//        if (message == null) {
//            message = "No message/exception is set";
//        }
//        if (Helper.isEmpty(message)) {
//            message = "Empty/NULL log message";
//        }

        int methodCount = getMethodCount();
        if(isFirstLineInCollapsedGroup) {
            msgBufferTagSuffix =tagSuffix;
            msgBufferLogLevel=loglevel;
            logTopBorder(loglevel, tagSuffix);
            boolean isShowThreadInfo = settings.isShowThreadInfo();
            logHeaderContent(loglevel, tagSuffix, methodCount, isShowThreadInfo, isShowCallStack);
            if (isShowCallStack&& methodCount > 0) {
                logDivider(loglevel, tagSuffix);
            }
        }
        msgbuffer.append(message);
    }
    private void flushCollapsedBuffer() {
        logMessageBodyChunked(msgBufferLogLevel, msgBufferTagSuffix, msgbuffer.toString());
        msgbuffer.setLength(0); //empty buffer
        msgBufferTagSuffix="";
        msgBufferLogLevel=-1;
    }

    private void logMessageBodyChunked(int loglevel, String tagSuffix, String message) {
        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        int CHUNK_SIZE=settings.getLogAdapter().max_message_size();
        if (length <= CHUNK_SIZE) {
            logContent(loglevel, tagSuffix, message);
            logBottomBorder(loglevel, tagSuffix);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(loglevel, tagSuffix, new String(bytes, i, count));
        }
        logBottomBorder(loglevel, tagSuffix);
    }

    public void resetSettings() {
        settings.reset();
    }

//    /**
//     * This method is synchronized in order to avoid messy of logs' order.
//     */
//    private synchronized void log(int priority, Throwable throwable, String msg, Object... args) {
//        if (!settings.isLogEnabled()) {
//            return;
//        }
//        String tag = getTag();
//        String message = createMessage(msg, args);
//        log(priority, tag, message, throwable);
//    }

    private void logTopBorder(int logType, String tagSuffix) {
        if (!settings.isBorderEnabled()) return;
        logChunk(logType, tagSuffix, TOP_BORDER);
    }
    private String HorizontalDoubleLine() {
        return settings.isBorderEnabled() ? HORIZONTAL_DOUBLE_LINE_STR : "";
    }
    @SuppressWarnings("StringBufferReplaceableByString")
    private void logHeaderContent(int logType, String tagSuffix, int methodCount,boolean isShowThreadInfo,boolean isShowCallStack) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (isShowThreadInfo) {
            logChunk(logType, tagSuffix, HorizontalDoubleLine() + "Thread: " + Thread.currentThread().getName());
            logDivider(logType, tagSuffix);
        }
        if(!isShowCallStack) return;
        String level = "";

        int stackOffset = getStackOffset(trace) + settings.getMethodOffset();

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("║ ")
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            logChunk(logType, tagSuffix, builder.toString());
        }
    }

    private void logBottomBorder(int logType, String tagSuffix) {
        if (!settings.isBorderEnabled()) return;
        logChunk(logType, tagSuffix, BOTTOM_BORDER);
    }

    private void logDivider(int logType, String tagSuffix) {
        if (!settings.isBorderEnabled()) return;
        logChunk(logType, tagSuffix, MIDDLE_BORDER);
    }

    private void logContent(int logType, String tagSuffix, String chunk) {
        String[] lines = chunk.split(LINE_SEPARATOR_CHAR);
        for (String line : lines) {
            logChunk(logType, tagSuffix, HorizontalDoubleLine() + line);
        }
    }


    private void logChunk(int logType, String tagSuffix, String chunk) {
        String finalTag = formatTag(tagSuffix);
        switch (logType) {
            case LogLevel.ERROR:
                settings.getLogAdapter().e(finalTag, chunk);
                break;
            case LogLevel.INFO:
                settings.getLogAdapter().i(finalTag, chunk);
                break;
            case LogLevel.VERBOSE:
                settings.getLogAdapter().v(finalTag, chunk);
                break;
            case LogLevel.WARN:
                settings.getLogAdapter().w(finalTag, chunk);
                break;
            case LogLevel.ASSERT:
                settings.getLogAdapter().wtf(finalTag, chunk);
                break;
            case LogLevel.DEBUG:
                // Fall through, log debug by default
            default:
                settings.getLogAdapter().d(finalTag, chunk);
                break;
        }
    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private String formatTag(String suffixTag) {
        if (!Helper.isEmpty(suffixTag) && !Helper.equals(this.tag, suffixTag)) {
            return this.tag + "-" + suffixTag + groupBlanks;
        }
        return this.tag;
    }

    /**
     * @return the appropriate tag based on local or global
     */
//    private String getTag() {
//        String tag = localTag.get();
//        if (tag != null) {
//            localTag.remove();
//            return tag;
//        }
//        return this.tag;
//    }

//    private String createMessage(String message, Object... args) {
//        return args == null || args.length == 0 ? message : String.format(message, args);
//    }

    private int getMethodCount() {
        int count = localMethodCount;
        int result = settings.getMethodCount();
        if (count >=0) {
            localMethodCount=-1;
            result = count;
        }
        if (result < 0) {
            throw new IllegalStateException("methodCount cannot be negative");
        }
        return result;
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LogFormatterPrinter.class.getName()) && !name.equals(LogFormatter.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

}
