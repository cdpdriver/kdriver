package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.debugger: Debugger
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Debugger(this))

/**
 * Debugger domain exposes JavaScript debugging capabilities. It allows setting and removing
 * breakpoints, stepping through execution, exploring stack traces, etc.
 */
public class Debugger(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when breakpoint is resolved to an actual script and location.
     * Deprecated in favor of `resolvedBreakpoints` in the `scriptParsed` event.
     */
    public val breakpointResolved: Flow<BreakpointResolvedParameter> = cdp
        .events
        .filter { it.method == "Debugger.breakpointResolved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when the virtual machine stopped on breakpoint or exception or any other stop criteria.
     */
    public val paused: Flow<PausedParameter> = cdp
        .events
        .filter { it.method == "Debugger.paused" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when the virtual machine resumed execution.
     */
    public val resumed: Flow<Unit> = cdp
        .events
        .filter { it.method == "Debugger.resumed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when virtual machine fails to parse the script.
     */
    public val scriptFailedToParse: Flow<ScriptFailedToParseParameter> = cdp
        .events
        .filter { it.method == "Debugger.scriptFailedToParse" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when virtual machine parses script. This event is also fired for all known and uncollected
     * scripts upon enabling debugger.
     */
    public val scriptParsed: Flow<ScriptParsedParameter> = cdp
        .events
        .filter { it.method == "Debugger.scriptParsed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Continues execution until specific location is reached.
     */
    public suspend fun continueToLocation(args: ContinueToLocationParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.continueToLocation", parameter)
    }

    /**
     * Continues execution until specific location is reached.
     *
     * @param location Location to continue to.
     * @param targetCallFrames No description
     */
    public suspend fun continueToLocation(location: Location, targetCallFrames: String? = null) {
        val parameter = ContinueToLocationParameter(location = location, targetCallFrames = targetCallFrames)
        continueToLocation(parameter)
    }

    /**
     * Disables debugger for given page.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Debugger.disable", parameter)
    }

    /**
     * Enables debugger for the given page. Clients should not assume that the debugging has been
     * enabled until the result for this command is received.
     */
    public suspend fun enable(args: EnableParameter): EnableReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.enable", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Enables debugger for the given page. Clients should not assume that the debugging has been
     * enabled until the result for this command is received.
     *
     * @param maxScriptsCacheSize The maximum size in bytes of collected scripts (not referenced by other heap objects)
     * the debugger can hold. Puts no limit if parameter is omitted.
     */
    public suspend fun enable(maxScriptsCacheSize: Double? = null): EnableReturn {
        val parameter = EnableParameter(maxScriptsCacheSize = maxScriptsCacheSize)
        return enable(parameter)
    }

    /**
     * Evaluates expression on a given call frame.
     */
    public suspend fun evaluateOnCallFrame(args: EvaluateOnCallFrameParameter): EvaluateOnCallFrameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.evaluateOnCallFrame", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Evaluates expression on a given call frame.
     *
     * @param callFrameId Call frame identifier to evaluate on.
     * @param expression Expression to evaluate.
     * @param objectGroup String object group name to put result into (allows rapid releasing resulting object handles
     * using `releaseObjectGroup`).
     * @param includeCommandLineAPI Specifies whether command line API should be available to the evaluated expression, defaults
     * to false.
     * @param silent In silent mode exceptions thrown during evaluation are not reported and do not pause
     * execution. Overrides `setPauseOnException` state.
     * @param returnByValue Whether the result is expected to be a JSON object that should be sent by value.
     * @param generatePreview Whether preview should be generated for the result.
     * @param throwOnSideEffect Whether to throw an exception if side effect cannot be ruled out during evaluation.
     * @param timeout Terminate execution after timing out (number of milliseconds).
     */
    public suspend fun evaluateOnCallFrame(
        callFrameId: String,
        expression: String,
        objectGroup: String? = null,
        includeCommandLineAPI: Boolean? = null,
        silent: Boolean? = null,
        returnByValue: Boolean? = null,
        generatePreview: Boolean? = null,
        throwOnSideEffect: Boolean? = null,
        timeout: Double? = null,
    ): EvaluateOnCallFrameReturn {
        val parameter = EvaluateOnCallFrameParameter(
            callFrameId = callFrameId,
            expression = expression,
            objectGroup = objectGroup,
            includeCommandLineAPI = includeCommandLineAPI,
            silent = silent,
            returnByValue = returnByValue,
            generatePreview = generatePreview,
            throwOnSideEffect = throwOnSideEffect,
            timeout = timeout
        )
        return evaluateOnCallFrame(parameter)
    }

    /**
     * Returns possible locations for breakpoint. scriptId in start and end range locations should be
     * the same.
     */
    public suspend fun getPossibleBreakpoints(args: GetPossibleBreakpointsParameter): GetPossibleBreakpointsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.getPossibleBreakpoints", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns possible locations for breakpoint. scriptId in start and end range locations should be
     * the same.
     *
     * @param start Start of range to search possible breakpoint locations in.
     * @param end End of range to search possible breakpoint locations in (excluding). When not specified, end
     * of scripts is used as end of range.
     * @param restrictToFunction Only consider locations which are in the same (non-nested) function as start.
     */
    public suspend fun getPossibleBreakpoints(
        start: Location,
        end: Location? = null,
        restrictToFunction: Boolean? = null,
    ): GetPossibleBreakpointsReturn {
        val parameter =
            GetPossibleBreakpointsParameter(start = start, end = end, restrictToFunction = restrictToFunction)
        return getPossibleBreakpoints(parameter)
    }

    /**
     * Returns source for the script with given id.
     */
    public suspend fun getScriptSource(args: GetScriptSourceParameter): GetScriptSourceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.getScriptSource", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns source for the script with given id.
     *
     * @param scriptId Id of the script to get source for.
     */
    public suspend fun getScriptSource(scriptId: String): GetScriptSourceReturn {
        val parameter = GetScriptSourceParameter(scriptId = scriptId)
        return getScriptSource(parameter)
    }

    public suspend fun disassembleWasmModule(args: DisassembleWasmModuleParameter): DisassembleWasmModuleReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.disassembleWasmModule", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param scriptId Id of the script to disassemble
     */
    public suspend fun disassembleWasmModule(scriptId: String): DisassembleWasmModuleReturn {
        val parameter = DisassembleWasmModuleParameter(scriptId = scriptId)
        return disassembleWasmModule(parameter)
    }

    /**
     * Disassemble the next chunk of lines for the module corresponding to the
     * stream. If disassembly is complete, this API will invalidate the streamId
     * and return an empty chunk. Any subsequent calls for the now invalid stream
     * will return errors.
     */
    public suspend fun nextWasmDisassemblyChunk(args: NextWasmDisassemblyChunkParameter): NextWasmDisassemblyChunkReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.nextWasmDisassemblyChunk", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Disassemble the next chunk of lines for the module corresponding to the
     * stream. If disassembly is complete, this API will invalidate the streamId
     * and return an empty chunk. Any subsequent calls for the now invalid stream
     * will return errors.
     *
     * @param streamId No description
     */
    public suspend fun nextWasmDisassemblyChunk(streamId: String): NextWasmDisassemblyChunkReturn {
        val parameter = NextWasmDisassemblyChunkParameter(streamId = streamId)
        return nextWasmDisassemblyChunk(parameter)
    }

    /**
     * This command is deprecated. Use getScriptSource instead.
     */
    @Deprecated(message = "")
    public suspend fun getWasmBytecode(args: GetWasmBytecodeParameter): GetWasmBytecodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.getWasmBytecode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * This command is deprecated. Use getScriptSource instead.
     *
     * @param scriptId Id of the Wasm script to get source for.
     */
    @Deprecated(message = "")
    public suspend fun getWasmBytecode(scriptId: String): GetWasmBytecodeReturn {
        val parameter = GetWasmBytecodeParameter(scriptId = scriptId)
        return getWasmBytecode(parameter)
    }

    /**
     * Returns stack trace with given `stackTraceId`.
     */
    public suspend fun getStackTrace(args: GetStackTraceParameter): GetStackTraceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.getStackTrace", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns stack trace with given `stackTraceId`.
     *
     * @param stackTraceId No description
     */
    public suspend fun getStackTrace(stackTraceId: Runtime.StackTraceId): GetStackTraceReturn {
        val parameter = GetStackTraceParameter(stackTraceId = stackTraceId)
        return getStackTrace(parameter)
    }

    /**
     * Stops on the next JavaScript statement.
     */
    public suspend fun pause() {
        val parameter = null
        cdp.callCommand("Debugger.pause", parameter)
    }

    @Deprecated(message = "")
    public suspend fun pauseOnAsyncCall(args: PauseOnAsyncCallParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.pauseOnAsyncCall", parameter)
    }

    /**
     *
     *
     * @param parentStackTraceId Debugger will pause when async call with given stack trace is started.
     */
    @Deprecated(message = "")
    public suspend fun pauseOnAsyncCall(parentStackTraceId: Runtime.StackTraceId) {
        val parameter = PauseOnAsyncCallParameter(parentStackTraceId = parentStackTraceId)
        pauseOnAsyncCall(parameter)
    }

    /**
     * Removes JavaScript breakpoint.
     */
    public suspend fun removeBreakpoint(args: RemoveBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.removeBreakpoint", parameter)
    }

    /**
     * Removes JavaScript breakpoint.
     *
     * @param breakpointId No description
     */
    public suspend fun removeBreakpoint(breakpointId: String) {
        val parameter = RemoveBreakpointParameter(breakpointId = breakpointId)
        removeBreakpoint(parameter)
    }

    /**
     * Restarts particular call frame from the beginning. The old, deprecated
     * behavior of `restartFrame` is to stay paused and allow further CDP commands
     * after a restart was scheduled. This can cause problems with restarting, so
     * we now continue execution immediatly after it has been scheduled until we
     * reach the beginning of the restarted frame.
     *
     * To stay back-wards compatible, `restartFrame` now expects a `mode`
     * parameter to be present. If the `mode` parameter is missing, `restartFrame`
     * errors out.
     *
     * The various return values are deprecated and `callFrames` is always empty.
     * Use the call frames from the `Debugger#paused` events instead, that fires
     * once V8 pauses at the beginning of the restarted function.
     */
    public suspend fun restartFrame(args: RestartFrameParameter): RestartFrameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.restartFrame", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Restarts particular call frame from the beginning. The old, deprecated
     * behavior of `restartFrame` is to stay paused and allow further CDP commands
     * after a restart was scheduled. This can cause problems with restarting, so
     * we now continue execution immediatly after it has been scheduled until we
     * reach the beginning of the restarted frame.
     *
     * To stay back-wards compatible, `restartFrame` now expects a `mode`
     * parameter to be present. If the `mode` parameter is missing, `restartFrame`
     * errors out.
     *
     * The various return values are deprecated and `callFrames` is always empty.
     * Use the call frames from the `Debugger#paused` events instead, that fires
     * once V8 pauses at the beginning of the restarted function.
     *
     * @param callFrameId Call frame identifier to evaluate on.
     * @param mode The `mode` parameter must be present and set to 'StepInto', otherwise
     * `restartFrame` will error out.
     */
    public suspend fun restartFrame(callFrameId: String, mode: String? = null): RestartFrameReturn {
        val parameter = RestartFrameParameter(callFrameId = callFrameId, mode = mode)
        return restartFrame(parameter)
    }

    /**
     * Resumes JavaScript execution.
     */
    public suspend fun resume(args: ResumeParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.resume", parameter)
    }

    /**
     * Resumes JavaScript execution.
     *
     * @param terminateOnResume Set to true to terminate execution upon resuming execution. In contrast
     * to Runtime.terminateExecution, this will allows to execute further
     * JavaScript (i.e. via evaluation) until execution of the paused code
     * is actually resumed, at which point termination is triggered.
     * If execution is currently not paused, this parameter has no effect.
     */
    public suspend fun resume(terminateOnResume: Boolean? = null) {
        val parameter = ResumeParameter(terminateOnResume = terminateOnResume)
        resume(parameter)
    }

    /**
     * Searches for given string in script content.
     */
    public suspend fun searchInContent(args: SearchInContentParameter): SearchInContentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.searchInContent", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Searches for given string in script content.
     *
     * @param scriptId Id of the script to search in.
     * @param query String to search for.
     * @param caseSensitive If true, search is case sensitive.
     * @param isRegex If true, treats string parameter as regex.
     */
    public suspend fun searchInContent(
        scriptId: String,
        query: String,
        caseSensitive: Boolean? = null,
        isRegex: Boolean? = null,
    ): SearchInContentReturn {
        val parameter = SearchInContentParameter(
            scriptId = scriptId,
            query = query,
            caseSensitive = caseSensitive,
            isRegex = isRegex
        )
        return searchInContent(parameter)
    }

    /**
     * Enables or disables async call stacks tracking.
     */
    public suspend fun setAsyncCallStackDepth(args: SetAsyncCallStackDepthParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setAsyncCallStackDepth", parameter)
    }

    /**
     * Enables or disables async call stacks tracking.
     *
     * @param maxDepth Maximum depth of async call stacks. Setting to `0` will effectively disable collecting async
     * call stacks (default).
     */
    public suspend fun setAsyncCallStackDepth(maxDepth: Int) {
        val parameter = SetAsyncCallStackDepthParameter(maxDepth = maxDepth)
        setAsyncCallStackDepth(parameter)
    }

    /**
     * Replace previous blackbox execution contexts with passed ones. Forces backend to skip
     * stepping/pausing in scripts in these execution contexts. VM will try to leave blackboxed script by
     * performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     */
    public suspend fun setBlackboxExecutionContexts(args: SetBlackboxExecutionContextsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setBlackboxExecutionContexts", parameter)
    }

    /**
     * Replace previous blackbox execution contexts with passed ones. Forces backend to skip
     * stepping/pausing in scripts in these execution contexts. VM will try to leave blackboxed script by
     * performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     *
     * @param uniqueIds Array of execution context unique ids for the debugger to ignore.
     */
    public suspend fun setBlackboxExecutionContexts(uniqueIds: List<String>) {
        val parameter = SetBlackboxExecutionContextsParameter(uniqueIds = uniqueIds)
        setBlackboxExecutionContexts(parameter)
    }

    /**
     * Replace previous blackbox patterns with passed ones. Forces backend to skip stepping/pausing in
     * scripts with url matching one of the patterns. VM will try to leave blackboxed script by
     * performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     */
    public suspend fun setBlackboxPatterns(args: SetBlackboxPatternsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setBlackboxPatterns", parameter)
    }

    /**
     * Replace previous blackbox patterns with passed ones. Forces backend to skip stepping/pausing in
     * scripts with url matching one of the patterns. VM will try to leave blackboxed script by
     * performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     *
     * @param patterns Array of regexps that will be used to check script url for blackbox state.
     * @param skipAnonymous If true, also ignore scripts with no source url.
     */
    public suspend fun setBlackboxPatterns(patterns: List<String>, skipAnonymous: Boolean? = null) {
        val parameter = SetBlackboxPatternsParameter(patterns = patterns, skipAnonymous = skipAnonymous)
        setBlackboxPatterns(parameter)
    }

    /**
     * Makes backend skip steps in the script in blackboxed ranges. VM will try leave blacklisted
     * scripts by performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     * Positions array contains positions where blackbox state is changed. First interval isn't
     * blackboxed. Array should be sorted.
     */
    public suspend fun setBlackboxedRanges(args: SetBlackboxedRangesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setBlackboxedRanges", parameter)
    }

    /**
     * Makes backend skip steps in the script in blackboxed ranges. VM will try leave blacklisted
     * scripts by performing 'step in' several times, finally resorting to 'step out' if unsuccessful.
     * Positions array contains positions where blackbox state is changed. First interval isn't
     * blackboxed. Array should be sorted.
     *
     * @param scriptId Id of the script.
     * @param positions No description
     */
    public suspend fun setBlackboxedRanges(scriptId: String, positions: List<ScriptPosition>) {
        val parameter = SetBlackboxedRangesParameter(scriptId = scriptId, positions = positions)
        setBlackboxedRanges(parameter)
    }

    /**
     * Sets JavaScript breakpoint at a given location.
     */
    public suspend fun setBreakpoint(args: SetBreakpointParameter): SetBreakpointReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.setBreakpoint", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets JavaScript breakpoint at a given location.
     *
     * @param location Location to set breakpoint in.
     * @param condition Expression to use as a breakpoint condition. When specified, debugger will only stop on the
     * breakpoint if this expression evaluates to true.
     */
    public suspend fun setBreakpoint(location: Location, condition: String? = null): SetBreakpointReturn {
        val parameter = SetBreakpointParameter(location = location, condition = condition)
        return setBreakpoint(parameter)
    }

    /**
     * Sets instrumentation breakpoint.
     */
    public suspend fun setInstrumentationBreakpoint(args: SetInstrumentationBreakpointParameter): SetInstrumentationBreakpointReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.setInstrumentationBreakpoint", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets instrumentation breakpoint.
     *
     * @param instrumentation Instrumentation name.
     */
    public suspend fun setInstrumentationBreakpoint(instrumentation: String): SetInstrumentationBreakpointReturn {
        val parameter = SetInstrumentationBreakpointParameter(instrumentation = instrumentation)
        return setInstrumentationBreakpoint(parameter)
    }

    /**
     * Sets JavaScript breakpoint at given location specified either by URL or URL regex. Once this
     * command is issued, all existing parsed scripts will have breakpoints resolved and returned in
     * `locations` property. Further matching script parsing will result in subsequent
     * `breakpointResolved` events issued. This logical breakpoint will survive page reloads.
     */
    public suspend fun setBreakpointByUrl(args: SetBreakpointByUrlParameter): SetBreakpointByUrlReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.setBreakpointByUrl", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets JavaScript breakpoint at given location specified either by URL or URL regex. Once this
     * command is issued, all existing parsed scripts will have breakpoints resolved and returned in
     * `locations` property. Further matching script parsing will result in subsequent
     * `breakpointResolved` events issued. This logical breakpoint will survive page reloads.
     *
     * @param lineNumber Line number to set breakpoint at.
     * @param url URL of the resources to set breakpoint on.
     * @param urlRegex Regex pattern for the URLs of the resources to set breakpoints on. Either `url` or
     * `urlRegex` must be specified.
     * @param scriptHash Script hash of the resources to set breakpoint on.
     * @param columnNumber Offset in the line to set breakpoint at.
     * @param condition Expression to use as a breakpoint condition. When specified, debugger will only stop on the
     * breakpoint if this expression evaluates to true.
     */
    public suspend fun setBreakpointByUrl(
        lineNumber: Int,
        url: String? = null,
        urlRegex: String? = null,
        scriptHash: String? = null,
        columnNumber: Int? = null,
        condition: String? = null,
    ): SetBreakpointByUrlReturn {
        val parameter = SetBreakpointByUrlParameter(
            lineNumber = lineNumber,
            url = url,
            urlRegex = urlRegex,
            scriptHash = scriptHash,
            columnNumber = columnNumber,
            condition = condition
        )
        return setBreakpointByUrl(parameter)
    }

    /**
     * Sets JavaScript breakpoint before each call to the given function.
     * If another function was created from the same source as a given one,
     * calling it will also trigger the breakpoint.
     */
    public suspend fun setBreakpointOnFunctionCall(args: SetBreakpointOnFunctionCallParameter): SetBreakpointOnFunctionCallReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.setBreakpointOnFunctionCall", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets JavaScript breakpoint before each call to the given function.
     * If another function was created from the same source as a given one,
     * calling it will also trigger the breakpoint.
     *
     * @param objectId Function object id.
     * @param condition Expression to use as a breakpoint condition. When specified, debugger will
     * stop on the breakpoint if this expression evaluates to true.
     */
    public suspend fun setBreakpointOnFunctionCall(
        objectId: String,
        condition: String? = null,
    ): SetBreakpointOnFunctionCallReturn {
        val parameter = SetBreakpointOnFunctionCallParameter(objectId = objectId, condition = condition)
        return setBreakpointOnFunctionCall(parameter)
    }

    /**
     * Activates / deactivates all breakpoints on the page.
     */
    public suspend fun setBreakpointsActive(args: SetBreakpointsActiveParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setBreakpointsActive", parameter)
    }

    /**
     * Activates / deactivates all breakpoints on the page.
     *
     * @param active New value for breakpoints active state.
     */
    public suspend fun setBreakpointsActive(active: Boolean) {
        val parameter = SetBreakpointsActiveParameter(active = active)
        setBreakpointsActive(parameter)
    }

    /**
     * Defines pause on exceptions state. Can be set to stop on all exceptions, uncaught exceptions,
     * or caught exceptions, no exceptions. Initial pause on exceptions state is `none`.
     */
    public suspend fun setPauseOnExceptions(args: SetPauseOnExceptionsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setPauseOnExceptions", parameter)
    }

    /**
     * Defines pause on exceptions state. Can be set to stop on all exceptions, uncaught exceptions,
     * or caught exceptions, no exceptions. Initial pause on exceptions state is `none`.
     *
     * @param state Pause on exceptions mode.
     */
    public suspend fun setPauseOnExceptions(state: String) {
        val parameter = SetPauseOnExceptionsParameter(state = state)
        setPauseOnExceptions(parameter)
    }

    /**
     * Changes return value in top frame. Available only at return break position.
     */
    public suspend fun setReturnValue(args: SetReturnValueParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setReturnValue", parameter)
    }

    /**
     * Changes return value in top frame. Available only at return break position.
     *
     * @param newValue New return value.
     */
    public suspend fun setReturnValue(newValue: Runtime.CallArgument) {
        val parameter = SetReturnValueParameter(newValue = newValue)
        setReturnValue(parameter)
    }

    /**
     * Edits JavaScript source live.
     *
     * In general, functions that are currently on the stack can not be edited with
     * a single exception: If the edited function is the top-most stack frame and
     * that is the only activation of that function on the stack. In this case
     * the live edit will be successful and a `Debugger.restartFrame` for the
     * top-most function is automatically triggered.
     */
    public suspend fun setScriptSource(args: SetScriptSourceParameter): SetScriptSourceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Debugger.setScriptSource", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Edits JavaScript source live.
     *
     * In general, functions that are currently on the stack can not be edited with
     * a single exception: If the edited function is the top-most stack frame and
     * that is the only activation of that function on the stack. In this case
     * the live edit will be successful and a `Debugger.restartFrame` for the
     * top-most function is automatically triggered.
     *
     * @param scriptId Id of the script to edit.
     * @param scriptSource New content of the script.
     * @param dryRun If true the change will not actually be applied. Dry run may be used to get result
     * description without actually modifying the code.
     * @param allowTopFrameEditing If true, then `scriptSource` is allowed to change the function on top of the stack
     * as long as the top-most stack frame is the only activation of that function.
     */
    public suspend fun setScriptSource(
        scriptId: String,
        scriptSource: String,
        dryRun: Boolean? = null,
        allowTopFrameEditing: Boolean? = null,
    ): SetScriptSourceReturn {
        val parameter = SetScriptSourceParameter(
            scriptId = scriptId,
            scriptSource = scriptSource,
            dryRun = dryRun,
            allowTopFrameEditing = allowTopFrameEditing
        )
        return setScriptSource(parameter)
    }

    /**
     * Makes page not interrupt on any pauses (breakpoint, exception, dom exception etc).
     */
    public suspend fun setSkipAllPauses(args: SetSkipAllPausesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setSkipAllPauses", parameter)
    }

    /**
     * Makes page not interrupt on any pauses (breakpoint, exception, dom exception etc).
     *
     * @param skip New value for skip pauses state.
     */
    public suspend fun setSkipAllPauses(skip: Boolean) {
        val parameter = SetSkipAllPausesParameter(skip = skip)
        setSkipAllPauses(parameter)
    }

    /**
     * Changes value of variable in a callframe. Object-based scopes are not supported and must be
     * mutated manually.
     */
    public suspend fun setVariableValue(args: SetVariableValueParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.setVariableValue", parameter)
    }

    /**
     * Changes value of variable in a callframe. Object-based scopes are not supported and must be
     * mutated manually.
     *
     * @param scopeNumber 0-based number of scope as was listed in scope chain. Only 'local', 'closure' and 'catch'
     * scope types are allowed. Other scopes could be manipulated manually.
     * @param variableName Variable name.
     * @param newValue New variable value.
     * @param callFrameId Id of callframe that holds variable.
     */
    public suspend fun setVariableValue(
        scopeNumber: Int,
        variableName: String,
        newValue: Runtime.CallArgument,
        callFrameId: String,
    ) {
        val parameter = SetVariableValueParameter(
            scopeNumber = scopeNumber,
            variableName = variableName,
            newValue = newValue,
            callFrameId = callFrameId
        )
        setVariableValue(parameter)
    }

    /**
     * Steps into the function call.
     */
    public suspend fun stepInto(args: StepIntoParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.stepInto", parameter)
    }

    /**
     * Steps into the function call.
     *
     * @param breakOnAsyncCall Debugger will pause on the execution of the first async task which was scheduled
     * before next pause.
     * @param skipList The skipList specifies location ranges that should be skipped on step into.
     */
    public suspend fun stepInto(breakOnAsyncCall: Boolean? = null, skipList: List<LocationRange>? = null) {
        val parameter = StepIntoParameter(breakOnAsyncCall = breakOnAsyncCall, skipList = skipList)
        stepInto(parameter)
    }

    /**
     * Steps out of the function call.
     */
    public suspend fun stepOut() {
        val parameter = null
        cdp.callCommand("Debugger.stepOut", parameter)
    }

    /**
     * Steps over the statement.
     */
    public suspend fun stepOver(args: StepOverParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Debugger.stepOver", parameter)
    }

    /**
     * Steps over the statement.
     *
     * @param skipList The skipList specifies location ranges that should be skipped on step over.
     */
    public suspend fun stepOver(skipList: List<LocationRange>? = null) {
        val parameter = StepOverParameter(skipList = skipList)
        stepOver(parameter)
    }

    /**
     * Location in the source code.
     */
    @Serializable
    public data class Location(
        /**
         * Script identifier as reported in the `Debugger.scriptParsed`.
         */
        public val scriptId: String,
        /**
         * Line number in the script (0-based).
         */
        public val lineNumber: Int,
        /**
         * Column number in the script (0-based).
         */
        public val columnNumber: Int? = null,
    )

    /**
     * Location in the source code.
     */
    @Serializable
    public data class ScriptPosition(
        public val lineNumber: Int,
        public val columnNumber: Int,
    )

    /**
     * Location range within one script.
     */
    @Serializable
    public data class LocationRange(
        public val scriptId: String,
        public val start: ScriptPosition,
        public val end: ScriptPosition,
    )

    /**
     * JavaScript call frame. Array of call frames form the call stack.
     */
    @Serializable
    public data class CallFrame(
        /**
         * Call frame identifier. This identifier is only valid while the virtual machine is paused.
         */
        public val callFrameId: String,
        /**
         * Name of the JavaScript function called on this call frame.
         */
        public val functionName: String,
        /**
         * Location in the source code.
         */
        public val functionLocation: Location? = null,
        /**
         * Location in the source code.
         */
        public val location: Location,
        /**
         * JavaScript script name or url.
         * Deprecated in favor of using the `location.scriptId` to resolve the URL via a previously
         * sent `Debugger.scriptParsed` event.
         */
        public val url: String,
        /**
         * Scope chain for this call frame.
         */
        public val scopeChain: List<Scope>,
        /**
         * `this` object for this call frame.
         */
        public val `this`: Runtime.RemoteObject,
        /**
         * The value being returned, if the function is at return point.
         */
        public val returnValue: Runtime.RemoteObject? = null,
        /**
         * Valid only while the VM is paused and indicates whether this frame
         * can be restarted or not. Note that a `true` value here does not
         * guarantee that Debugger#restartFrame with this CallFrameId will be
         * successful, but it is very likely.
         */
        public val canBeRestarted: Boolean? = null,
    )

    /**
     * Scope description.
     */
    @Serializable
    public data class Scope(
        /**
         * Scope type.
         */
        public val type: String,
        /**
         * Object representing the scope. For `global` and `with` scopes it represents the actual
         * object; for the rest of the scopes, it is artificial transient object enumerating scope
         * variables as its properties.
         */
        public val `object`: Runtime.RemoteObject,
        public val name: String? = null,
        /**
         * Location in the source code where scope starts
         */
        public val startLocation: Location? = null,
        /**
         * Location in the source code where scope ends
         */
        public val endLocation: Location? = null,
    )

    /**
     * Search match for resource.
     */
    @Serializable
    public data class SearchMatch(
        /**
         * Line number in resource content.
         */
        public val lineNumber: Double,
        /**
         * Line with match content.
         */
        public val lineContent: String,
    )

    @Serializable
    public data class BreakLocation(
        /**
         * Script identifier as reported in the `Debugger.scriptParsed`.
         */
        public val scriptId: String,
        /**
         * Line number in the script (0-based).
         */
        public val lineNumber: Int,
        /**
         * Column number in the script (0-based).
         */
        public val columnNumber: Int? = null,
        public val type: String? = null,
    )

    @Serializable
    public data class WasmDisassemblyChunk(
        /**
         * The next chunk of disassembled lines.
         */
        public val lines: List<String>,
        /**
         * The bytecode offsets describing the start of each line.
         */
        public val bytecodeOffsets: List<Int>,
    )

    /**
     * Enum of possible script languages.
     */
    @Serializable
    public enum class ScriptLanguage {
        @SerialName("JavaScript")
        JAVASCRIPT,

        @SerialName("WebAssembly")
        WEBASSEMBLY,
    }

    /**
     * Debug symbols available for a wasm script.
     */
    @Serializable
    public data class DebugSymbols(
        /**
         * Type of the debug symbols.
         */
        public val type: String,
        /**
         * URL of the external symbol source.
         */
        public val externalURL: String? = null,
    )

    @Serializable
    public data class ResolvedBreakpoint(
        /**
         * Breakpoint unique identifier.
         */
        public val breakpointId: String,
        /**
         * Actual breakpoint location.
         */
        public val location: Location,
    )

    /**
     * Fired when breakpoint is resolved to an actual script and location.
     * Deprecated in favor of `resolvedBreakpoints` in the `scriptParsed` event.
     */
    @Serializable
    public data class BreakpointResolvedParameter(
        /**
         * Breakpoint unique identifier.
         */
        public val breakpointId: String,
        /**
         * Actual breakpoint location.
         */
        public val location: Location,
    )

    /**
     * Fired when the virtual machine stopped on breakpoint or exception or any other stop criteria.
     */
    @Serializable
    public data class PausedParameter(
        /**
         * Call stack the virtual machine stopped on.
         */
        public val callFrames: List<CallFrame>,
        /**
         * Pause reason.
         */
        public val reason: String,
        /**
         * Object containing break-specific auxiliary properties.
         */
        public val `data`: Map<String, JsonElement>? = null,
        /**
         * Hit breakpoints IDs
         */
        public val hitBreakpoints: List<String>? = null,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTrace: Runtime.StackTrace? = null,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTraceId: Runtime.StackTraceId? = null,
        /**
         * Never present, will be removed.
         */
        public val asyncCallStackTraceId: Runtime.StackTraceId? = null,
    )

    /**
     * Fired when virtual machine fails to parse the script.
     */
    @Serializable
    public data class ScriptFailedToParseParameter(
        /**
         * Identifier of the script parsed.
         */
        public val scriptId: String,
        /**
         * URL or name of the script parsed (if any).
         */
        public val url: String,
        /**
         * Line offset of the script within the resource with given URL (for script tags).
         */
        public val startLine: Int,
        /**
         * Column offset of the script within the resource with given URL.
         */
        public val startColumn: Int,
        /**
         * Last line of the script.
         */
        public val endLine: Int,
        /**
         * Length of the last line of the script.
         */
        public val endColumn: Int,
        /**
         * Specifies script creation context.
         */
        public val executionContextId: Int,
        /**
         * Content hash of the script, SHA-256.
         */
        public val hash: String,
        /**
         * For Wasm modules, the content of the `build_id` custom section. For JavaScript the `debugId` magic comment.
         */
        public val buildId: String,
        /**
         * Embedder-specific auxiliary data likely matching {isDefault: boolean, type: 'default'|'isolated'|'worker', frameId: string}
         */
        public val executionContextAuxData: Map<String, JsonElement>? = null,
        /**
         * URL of source map associated with script (if any).
         */
        public val sourceMapURL: String? = null,
        /**
         * True, if this script has sourceURL.
         */
        public val hasSourceURL: Boolean? = null,
        /**
         * True, if this script is ES6 module.
         */
        public val isModule: Boolean? = null,
        /**
         * This script length.
         */
        public val length: Int? = null,
        /**
         * JavaScript top stack frame of where the script parsed event was triggered if available.
         */
        public val stackTrace: Runtime.StackTrace? = null,
        /**
         * If the scriptLanguage is WebAssembly, the code section offset in the module.
         */
        public val codeOffset: Int? = null,
        /**
         * The language of the script.
         */
        public val scriptLanguage: ScriptLanguage? = null,
        /**
         * The name the embedder supplied for this script.
         */
        public val embedderName: String? = null,
    )

    /**
     * Fired when virtual machine parses script. This event is also fired for all known and uncollected
     * scripts upon enabling debugger.
     */
    @Serializable
    public data class ScriptParsedParameter(
        /**
         * Identifier of the script parsed.
         */
        public val scriptId: String,
        /**
         * URL or name of the script parsed (if any).
         */
        public val url: String,
        /**
         * Line offset of the script within the resource with given URL (for script tags).
         */
        public val startLine: Int,
        /**
         * Column offset of the script within the resource with given URL.
         */
        public val startColumn: Int,
        /**
         * Last line of the script.
         */
        public val endLine: Int,
        /**
         * Length of the last line of the script.
         */
        public val endColumn: Int,
        /**
         * Specifies script creation context.
         */
        public val executionContextId: Int,
        /**
         * Content hash of the script, SHA-256.
         */
        public val hash: String,
        /**
         * For Wasm modules, the content of the `build_id` custom section. For JavaScript the `debugId` magic comment.
         */
        public val buildId: String,
        /**
         * Embedder-specific auxiliary data likely matching {isDefault: boolean, type: 'default'|'isolated'|'worker', frameId: string}
         */
        public val executionContextAuxData: Map<String, JsonElement>? = null,
        /**
         * True, if this script is generated as a result of the live edit operation.
         */
        public val isLiveEdit: Boolean? = null,
        /**
         * URL of source map associated with script (if any).
         */
        public val sourceMapURL: String? = null,
        /**
         * True, if this script has sourceURL.
         */
        public val hasSourceURL: Boolean? = null,
        /**
         * True, if this script is ES6 module.
         */
        public val isModule: Boolean? = null,
        /**
         * This script length.
         */
        public val length: Int? = null,
        /**
         * JavaScript top stack frame of where the script parsed event was triggered if available.
         */
        public val stackTrace: Runtime.StackTrace? = null,
        /**
         * If the scriptLanguage is WebAssembly, the code section offset in the module.
         */
        public val codeOffset: Int? = null,
        /**
         * The language of the script.
         */
        public val scriptLanguage: ScriptLanguage? = null,
        /**
         * If the scriptLanguage is WebAssembly, the source of debug symbols for the module.
         */
        public val debugSymbols: List<DebugSymbols>? = null,
        /**
         * The name the embedder supplied for this script.
         */
        public val embedderName: String? = null,
        /**
         * The list of set breakpoints in this script if calls to `setBreakpointByUrl`
         * matches this script's URL or hash. Clients that use this list can ignore the
         * `breakpointResolved` event. They are equivalent.
         */
        public val resolvedBreakpoints: List<ResolvedBreakpoint>? = null,
    )

    @Serializable
    public data class ContinueToLocationParameter(
        /**
         * Location to continue to.
         */
        public val location: Location,
        public val targetCallFrames: String? = null,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * The maximum size in bytes of collected scripts (not referenced by other heap objects)
         * the debugger can hold. Puts no limit if parameter is omitted.
         */
        public val maxScriptsCacheSize: Double? = null,
    )

    @Serializable
    public data class EnableReturn(
        /**
         * Unique identifier of the debugger.
         */
        public val debuggerId: String,
    )

    @Serializable
    public data class EvaluateOnCallFrameParameter(
        /**
         * Call frame identifier to evaluate on.
         */
        public val callFrameId: String,
        /**
         * Expression to evaluate.
         */
        public val expression: String,
        /**
         * String object group name to put result into (allows rapid releasing resulting object handles
         * using `releaseObjectGroup`).
         */
        public val objectGroup: String? = null,
        /**
         * Specifies whether command line API should be available to the evaluated expression, defaults
         * to false.
         */
        public val includeCommandLineAPI: Boolean? = null,
        /**
         * In silent mode exceptions thrown during evaluation are not reported and do not pause
         * execution. Overrides `setPauseOnException` state.
         */
        public val silent: Boolean? = null,
        /**
         * Whether the result is expected to be a JSON object that should be sent by value.
         */
        public val returnByValue: Boolean? = null,
        /**
         * Whether preview should be generated for the result.
         */
        public val generatePreview: Boolean? = null,
        /**
         * Whether to throw an exception if side effect cannot be ruled out during evaluation.
         */
        public val throwOnSideEffect: Boolean? = null,
        /**
         * Terminate execution after timing out (number of milliseconds).
         */
        public val timeout: Double? = null,
    )

    @Serializable
    public data class EvaluateOnCallFrameReturn(
        /**
         * Object wrapper for the evaluation result.
         */
        public val result: Runtime.RemoteObject,
        /**
         * Exception details.
         */
        public val exceptionDetails: Runtime.ExceptionDetails?,
    )

    @Serializable
    public data class GetPossibleBreakpointsParameter(
        /**
         * Start of range to search possible breakpoint locations in.
         */
        public val start: Location,
        /**
         * End of range to search possible breakpoint locations in (excluding). When not specified, end
         * of scripts is used as end of range.
         */
        public val end: Location? = null,
        /**
         * Only consider locations which are in the same (non-nested) function as start.
         */
        public val restrictToFunction: Boolean? = null,
    )

    @Serializable
    public data class GetPossibleBreakpointsReturn(
        /**
         * List of the possible breakpoint locations.
         */
        public val locations: List<BreakLocation>,
    )

    @Serializable
    public data class GetScriptSourceParameter(
        /**
         * Id of the script to get source for.
         */
        public val scriptId: String,
    )

    @Serializable
    public data class GetScriptSourceReturn(
        /**
         * Script source (empty in case of Wasm bytecode).
         */
        public val scriptSource: String,
        /**
         * Wasm bytecode. (Encoded as a base64 string when passed over JSON)
         */
        public val bytecode: String?,
    )

    @Serializable
    public data class DisassembleWasmModuleParameter(
        /**
         * Id of the script to disassemble
         */
        public val scriptId: String,
    )

    @Serializable
    public data class DisassembleWasmModuleReturn(
        /**
         * For large modules, return a stream from which additional chunks of
         * disassembly can be read successively.
         */
        public val streamId: String?,
        /**
         * The total number of lines in the disassembly text.
         */
        public val totalNumberOfLines: Int,
        /**
         * The offsets of all function bodies, in the format [start1, end1,
         * start2, end2, ...] where all ends are exclusive.
         */
        public val functionBodyOffsets: List<Int>,
        /**
         * The first chunk of disassembly.
         */
        public val chunk: WasmDisassemblyChunk,
    )

    @Serializable
    public data class NextWasmDisassemblyChunkParameter(
        public val streamId: String,
    )

    @Serializable
    public data class NextWasmDisassemblyChunkReturn(
        /**
         * The next chunk of disassembly.
         */
        public val chunk: WasmDisassemblyChunk,
    )

    @Serializable
    public data class GetWasmBytecodeParameter(
        /**
         * Id of the Wasm script to get source for.
         */
        public val scriptId: String,
    )

    @Serializable
    public data class GetWasmBytecodeReturn(
        /**
         * Script source. (Encoded as a base64 string when passed over JSON)
         */
        public val bytecode: String,
    )

    @Serializable
    public data class GetStackTraceParameter(
        public val stackTraceId: Runtime.StackTraceId,
    )

    @Serializable
    public data class GetStackTraceReturn(
        public val stackTrace: Runtime.StackTrace,
    )

    @Serializable
    public data class PauseOnAsyncCallParameter(
        /**
         * Debugger will pause when async call with given stack trace is started.
         */
        public val parentStackTraceId: Runtime.StackTraceId,
    )

    @Serializable
    public data class RemoveBreakpointParameter(
        public val breakpointId: String,
    )

    @Serializable
    public data class RestartFrameParameter(
        /**
         * Call frame identifier to evaluate on.
         */
        public val callFrameId: String,
        /**
         * The `mode` parameter must be present and set to 'StepInto', otherwise
         * `restartFrame` will error out.
         */
        public val mode: String? = null,
    )

    @Serializable
    public data class RestartFrameReturn(
        /**
         * New stack trace.
         */
        public val callFrames: List<CallFrame>,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTrace: Runtime.StackTrace?,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTraceId: Runtime.StackTraceId?,
    )

    @Serializable
    public data class ResumeParameter(
        /**
         * Set to true to terminate execution upon resuming execution. In contrast
         * to Runtime.terminateExecution, this will allows to execute further
         * JavaScript (i.e. via evaluation) until execution of the paused code
         * is actually resumed, at which point termination is triggered.
         * If execution is currently not paused, this parameter has no effect.
         */
        public val terminateOnResume: Boolean? = null,
    )

    @Serializable
    public data class SearchInContentParameter(
        /**
         * Id of the script to search in.
         */
        public val scriptId: String,
        /**
         * String to search for.
         */
        public val query: String,
        /**
         * If true, search is case sensitive.
         */
        public val caseSensitive: Boolean? = null,
        /**
         * If true, treats string parameter as regex.
         */
        public val isRegex: Boolean? = null,
    )

    @Serializable
    public data class SearchInContentReturn(
        /**
         * List of search matches.
         */
        public val result: List<SearchMatch>,
    )

    @Serializable
    public data class SetAsyncCallStackDepthParameter(
        /**
         * Maximum depth of async call stacks. Setting to `0` will effectively disable collecting async
         * call stacks (default).
         */
        public val maxDepth: Int,
    )

    @Serializable
    public data class SetBlackboxExecutionContextsParameter(
        /**
         * Array of execution context unique ids for the debugger to ignore.
         */
        public val uniqueIds: List<String>,
    )

    @Serializable
    public data class SetBlackboxPatternsParameter(
        /**
         * Array of regexps that will be used to check script url for blackbox state.
         */
        public val patterns: List<String>,
        /**
         * If true, also ignore scripts with no source url.
         */
        public val skipAnonymous: Boolean? = null,
    )

    @Serializable
    public data class SetBlackboxedRangesParameter(
        /**
         * Id of the script.
         */
        public val scriptId: String,
        public val positions: List<ScriptPosition>,
    )

    @Serializable
    public data class SetBreakpointParameter(
        /**
         * Location to set breakpoint in.
         */
        public val location: Location,
        /**
         * Expression to use as a breakpoint condition. When specified, debugger will only stop on the
         * breakpoint if this expression evaluates to true.
         */
        public val condition: String? = null,
    )

    @Serializable
    public data class SetBreakpointReturn(
        /**
         * Id of the created breakpoint for further reference.
         */
        public val breakpointId: String,
        /**
         * Location this breakpoint resolved into.
         */
        public val actualLocation: Location,
    )

    @Serializable
    public data class SetInstrumentationBreakpointParameter(
        /**
         * Instrumentation name.
         */
        public val instrumentation: String,
    )

    @Serializable
    public data class SetInstrumentationBreakpointReturn(
        /**
         * Id of the created breakpoint for further reference.
         */
        public val breakpointId: String,
    )

    @Serializable
    public data class SetBreakpointByUrlParameter(
        /**
         * Line number to set breakpoint at.
         */
        public val lineNumber: Int,
        /**
         * URL of the resources to set breakpoint on.
         */
        public val url: String? = null,
        /**
         * Regex pattern for the URLs of the resources to set breakpoints on. Either `url` or
         * `urlRegex` must be specified.
         */
        public val urlRegex: String? = null,
        /**
         * Script hash of the resources to set breakpoint on.
         */
        public val scriptHash: String? = null,
        /**
         * Offset in the line to set breakpoint at.
         */
        public val columnNumber: Int? = null,
        /**
         * Expression to use as a breakpoint condition. When specified, debugger will only stop on the
         * breakpoint if this expression evaluates to true.
         */
        public val condition: String? = null,
    )

    @Serializable
    public data class SetBreakpointByUrlReturn(
        /**
         * Id of the created breakpoint for further reference.
         */
        public val breakpointId: String,
        /**
         * List of the locations this breakpoint resolved into upon addition.
         */
        public val locations: List<Location>,
    )

    @Serializable
    public data class SetBreakpointOnFunctionCallParameter(
        /**
         * Function object id.
         */
        public val objectId: String,
        /**
         * Expression to use as a breakpoint condition. When specified, debugger will
         * stop on the breakpoint if this expression evaluates to true.
         */
        public val condition: String? = null,
    )

    @Serializable
    public data class SetBreakpointOnFunctionCallReturn(
        /**
         * Id of the created breakpoint for further reference.
         */
        public val breakpointId: String,
    )

    @Serializable
    public data class SetBreakpointsActiveParameter(
        /**
         * New value for breakpoints active state.
         */
        public val active: Boolean,
    )

    @Serializable
    public data class SetPauseOnExceptionsParameter(
        /**
         * Pause on exceptions mode.
         */
        public val state: String,
    )

    @Serializable
    public data class SetReturnValueParameter(
        /**
         * New return value.
         */
        public val newValue: Runtime.CallArgument,
    )

    @Serializable
    public data class SetScriptSourceParameter(
        /**
         * Id of the script to edit.
         */
        public val scriptId: String,
        /**
         * New content of the script.
         */
        public val scriptSource: String,
        /**
         * If true the change will not actually be applied. Dry run may be used to get result
         * description without actually modifying the code.
         */
        public val dryRun: Boolean? = null,
        /**
         * If true, then `scriptSource` is allowed to change the function on top of the stack
         * as long as the top-most stack frame is the only activation of that function.
         */
        public val allowTopFrameEditing: Boolean? = null,
    )

    @Serializable
    public data class SetScriptSourceReturn(
        /**
         * New stack trace in case editing has happened while VM was stopped.
         */
        public val callFrames: List<CallFrame>?,
        /**
         * Whether current call stack  was modified after applying the changes.
         */
        public val stackChanged: Boolean?,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTrace: Runtime.StackTrace?,
        /**
         * Async stack trace, if any.
         */
        public val asyncStackTraceId: Runtime.StackTraceId?,
        /**
         * Whether the operation was successful or not. Only `Ok` denotes a
         * successful live edit while the other enum variants denote why
         * the live edit failed.
         */
        public val status: String,
        /**
         * Exception details if any. Only present when `status` is `CompileError`.
         */
        public val exceptionDetails: Runtime.ExceptionDetails?,
    )

    @Serializable
    public data class SetSkipAllPausesParameter(
        /**
         * New value for skip pauses state.
         */
        public val skip: Boolean,
    )

    @Serializable
    public data class SetVariableValueParameter(
        /**
         * 0-based number of scope as was listed in scope chain. Only 'local', 'closure' and 'catch'
         * scope types are allowed. Other scopes could be manipulated manually.
         */
        public val scopeNumber: Int,
        /**
         * Variable name.
         */
        public val variableName: String,
        /**
         * New variable value.
         */
        public val newValue: Runtime.CallArgument,
        /**
         * Id of callframe that holds variable.
         */
        public val callFrameId: String,
    )

    @Serializable
    public data class StepIntoParameter(
        /**
         * Debugger will pause on the execution of the first async task which was scheduled
         * before next pause.
         */
        public val breakOnAsyncCall: Boolean? = null,
        /**
         * The skipList specifies location ranges that should be skipped on step into.
         */
        public val skipList: List<LocationRange>? = null,
    )

    @Serializable
    public data class StepOverParameter(
        /**
         * The skipList specifies location ranges that should be skipped on step over.
         */
        public val skipList: List<LocationRange>? = null,
    )
}
