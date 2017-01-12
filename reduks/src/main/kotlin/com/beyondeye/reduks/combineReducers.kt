package com.beyondeye.reduks

@SafeVarargs
fun <S> combineReducers(vararg reducers: Reducer<S>): Reducer<S> {
    return ReducerFn<S> { state: S, action: Any ->
        reducers.fold(state, { state, reducer -> reducer.reduce(state, action) })
//        var state = state
//        for (reducer in reducers) {
//            state = reducer.reduce(state, action)
//        }
//        state
    }
}

@SafeVarargs
fun <S> Reducer<S>.combinedWidth(vararg reducers: Reducer<S>):Reducer<S> {
    return ReducerFn<S> { state:S, action:Any ->
        val sAfterFirstReducer=this.reduce(state,action)
        reducers.fold(sAfterFirstReducer, { state, reducer -> reducer.reduce(state, action) })
    }
}