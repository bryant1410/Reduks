package com.beyondeye.reduks.logger

import com.beyondeye.reduks.logger.logformatter.LogFormatter


/**
 * logger that support grouping, i.e. common indenting of multiple subsequent lines in a similar way to javascript console:
 * see https://developer.mozilla.org/en/docs/Web/API/console (Using groups in console)
 * Created by daely on 7/25/2016.
 */
internal class DefaultGroupedLogger(tag:String): GroupedLogger {
    val logFormatter=LogFormatter.init(tag)
    override fun group(s: String,logLevel:Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun groupCollapsed(s: String,logLevel:Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun groupEnd() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun log(s: String, logLevel: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun json(objName:String,s: String, logLevel: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}