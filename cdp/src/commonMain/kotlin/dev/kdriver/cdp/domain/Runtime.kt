package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.runtime: Runtime
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Runtime(this))

/**
 * Runtime domain exposes JavaScript runtime by means of remote evaluation and mirror objects.
 * Evaluation results are returned as mirror object that expose object type, string representation
 * and unique identifier that can be used for further object reference. Original objects are
 * maintained in memory unless they are either explicitly released or are released along with the
 * other objects in their object group.
 */
public class Runtime(
    private val cdp: CDP,
) : Domain {
    public val bindingCalled: Flow<BindingCalledParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.bindingCalled"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val consoleAPICalled: Flow<ConsoleAPICalledParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.consoleAPICalled"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val exceptionRevoked: Flow<ExceptionRevokedParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.exceptionRevoked"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val exceptionThrown: Flow<ExceptionThrownParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.exceptionThrown"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val executionContextCreated: Flow<ExecutionContextCreatedParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.executionContextCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val executionContextDestroyed: Flow<ExecutionContextDestroyedParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.executionContextDestroyed"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val executionContextsCleared: Flow<Unit> = cdp
        .events
        .filter {
            it.method == "Runtime.executionContextsCleared"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val inspectRequested: Flow<InspectRequestedParameter> = cdp
        .events
        .filter {
            it.method == "Runtime.inspectRequested"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Add handler to promise with given promise object id.
     */
    public suspend fun awaitPromise(args: AwaitPromiseParameter): AwaitPromiseReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.awaitPromise", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Add handler to promise with given promise object id.
     */
    public suspend fun awaitPromise(
        promiseObjectId: String,
        returnByValue: Boolean? = null,
        generatePreview: Boolean? = null,
    ): AwaitPromiseReturn {
        val parameter = AwaitPromiseParameter(
            promiseObjectId = promiseObjectId,
            returnByValue = returnByValue,
            generatePreview = generatePreview
        )
        return awaitPromise(parameter)
    }

    /**
     * Calls function with given declaration on the given object. Object group of the result is
     * inherited from the target object.
     */
    public suspend fun callFunctionOn(args: CallFunctionOnParameter): CallFunctionOnReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.callFunctionOn", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Calls function with given declaration on the given object. Object group of the result is
     * inherited from the target object.
     */
    public suspend fun callFunctionOn(
        functionDeclaration: String,
        objectId: String? = null,
        arguments: List<CallArgument>? = null,
        silent: Boolean? = null,
        returnByValue: Boolean? = null,
        generatePreview: Boolean? = null,
        userGesture: Boolean? = null,
        awaitPromise: Boolean? = null,
        executionContextId: Int? = null,
        objectGroup: String? = null,
        throwOnSideEffect: Boolean? = null,
        uniqueContextId: String? = null,
        serializationOptions: SerializationOptions? = null,
    ): CallFunctionOnReturn {
        val parameter = CallFunctionOnParameter(
            functionDeclaration = functionDeclaration,
            objectId = objectId,
            arguments = arguments,
            silent = silent,
            returnByValue = returnByValue,
            generatePreview = generatePreview,
            userGesture = userGesture,
            awaitPromise = awaitPromise,
            executionContextId = executionContextId,
            objectGroup = objectGroup,
            throwOnSideEffect = throwOnSideEffect,
            uniqueContextId = uniqueContextId,
            serializationOptions = serializationOptions
        )
        return callFunctionOn(parameter)
    }

    /**
     * Compiles expression.
     */
    public suspend fun compileScript(args: CompileScriptParameter): CompileScriptReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.compileScript", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Compiles expression.
     */
    public suspend fun compileScript(
        expression: String,
        sourceURL: String,
        persistScript: Boolean,
        executionContextId: Int? = null,
    ): CompileScriptReturn {
        val parameter = CompileScriptParameter(
            expression = expression,
            sourceURL = sourceURL,
            persistScript = persistScript,
            executionContextId = executionContextId
        )
        return compileScript(parameter)
    }

    /**
     * Disables reporting of execution contexts creation.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Runtime.disable", parameter)
    }

    /**
     * Discards collected exceptions and console API calls.
     */
    public suspend fun discardConsoleEntries() {
        val parameter = null
        cdp.callCommand("Runtime.discardConsoleEntries", parameter)
    }

    /**
     * Enables reporting of execution contexts creation by means of `executionContextCreated` event.
     * When the reporting gets enabled the event will be sent immediately for each existing execution
     * context.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Runtime.enable", parameter)
    }

    /**
     * Evaluates expression on global object.
     */
    public suspend fun evaluate(args: EvaluateParameter): EvaluateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.evaluate", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Evaluates expression on global object.
     */
    public suspend fun evaluate(
        expression: String,
        objectGroup: String? = null,
        includeCommandLineAPI: Boolean? = null,
        silent: Boolean? = null,
        contextId: Int? = null,
        returnByValue: Boolean? = null,
        generatePreview: Boolean? = null,
        userGesture: Boolean? = null,
        awaitPromise: Boolean? = null,
        throwOnSideEffect: Boolean? = null,
        timeout: Double? = null,
        disableBreaks: Boolean? = null,
        replMode: Boolean? = null,
        allowUnsafeEvalBlockedByCSP: Boolean? = null,
        uniqueContextId: String? = null,
        serializationOptions: SerializationOptions? = null,
    ): EvaluateReturn {
        val parameter = EvaluateParameter(
            expression = expression,
            objectGroup = objectGroup,
            includeCommandLineAPI = includeCommandLineAPI,
            silent = silent,
            contextId = contextId,
            returnByValue = returnByValue,
            generatePreview = generatePreview,
            userGesture = userGesture,
            awaitPromise = awaitPromise,
            throwOnSideEffect = throwOnSideEffect,
            timeout = timeout,
            disableBreaks = disableBreaks,
            replMode = replMode,
            allowUnsafeEvalBlockedByCSP = allowUnsafeEvalBlockedByCSP,
            uniqueContextId = uniqueContextId,
            serializationOptions = serializationOptions
        )
        return evaluate(parameter)
    }

    /**
     * Returns the isolate id.
     */
    public suspend fun getIsolateId(): GetIsolateIdReturn {
        val parameter = null
        val result = cdp.callCommand("Runtime.getIsolateId", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the JavaScript heap usage.
     * It is the total usage of the corresponding isolate not scoped to a particular Runtime.
     */
    public suspend fun getHeapUsage(): GetHeapUsageReturn {
        val parameter = null
        val result = cdp.callCommand("Runtime.getHeapUsage", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns properties of a given object. Object group of the result is inherited from the target
     * object.
     */
    public suspend fun getProperties(args: GetPropertiesParameter): GetPropertiesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.getProperties", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns properties of a given object. Object group of the result is inherited from the target
     * object.
     */
    public suspend fun getProperties(
        objectId: String,
        ownProperties: Boolean? = null,
        accessorPropertiesOnly: Boolean? = null,
        generatePreview: Boolean? = null,
        nonIndexedPropertiesOnly: Boolean? = null,
    ): GetPropertiesReturn {
        val parameter = GetPropertiesParameter(
            objectId = objectId,
            ownProperties = ownProperties,
            accessorPropertiesOnly = accessorPropertiesOnly,
            generatePreview = generatePreview,
            nonIndexedPropertiesOnly = nonIndexedPropertiesOnly
        )
        return getProperties(parameter)
    }

    /**
     * Returns all let, const and class variables from global scope.
     */
    public suspend fun globalLexicalScopeNames(args: GlobalLexicalScopeNamesParameter): GlobalLexicalScopeNamesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.globalLexicalScopeNames", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all let, const and class variables from global scope.
     */
    public suspend fun globalLexicalScopeNames(executionContextId: Int? = null): GlobalLexicalScopeNamesReturn {
        val parameter = GlobalLexicalScopeNamesParameter(executionContextId = executionContextId)
        return globalLexicalScopeNames(parameter)
    }

    public suspend fun queryObjects(args: QueryObjectsParameter): QueryObjectsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.queryObjects", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun queryObjects(prototypeObjectId: String, objectGroup: String? = null): QueryObjectsReturn {
        val parameter = QueryObjectsParameter(prototypeObjectId = prototypeObjectId, objectGroup = objectGroup)
        return queryObjects(parameter)
    }

    /**
     * Releases remote object with given id.
     */
    public suspend fun releaseObject(args: ReleaseObjectParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.releaseObject", parameter)
    }

    /**
     * Releases remote object with given id.
     */
    public suspend fun releaseObject(objectId: String) {
        val parameter = ReleaseObjectParameter(objectId = objectId)
        releaseObject(parameter)
    }

    /**
     * Releases all remote objects that belong to a given group.
     */
    public suspend fun releaseObjectGroup(args: ReleaseObjectGroupParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.releaseObjectGroup", parameter)
    }

    /**
     * Releases all remote objects that belong to a given group.
     */
    public suspend fun releaseObjectGroup(objectGroup: String) {
        val parameter = ReleaseObjectGroupParameter(objectGroup = objectGroup)
        releaseObjectGroup(parameter)
    }

    /**
     * Tells inspected instance to run if it was waiting for debugger to attach.
     */
    public suspend fun runIfWaitingForDebugger() {
        val parameter = null
        cdp.callCommand("Runtime.runIfWaitingForDebugger", parameter)
    }

    /**
     * Runs script with given id in a given context.
     */
    public suspend fun runScript(args: RunScriptParameter): RunScriptReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.runScript", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Runs script with given id in a given context.
     */
    public suspend fun runScript(
        scriptId: String,
        executionContextId: Int? = null,
        objectGroup: String? = null,
        silent: Boolean? = null,
        includeCommandLineAPI: Boolean? = null,
        returnByValue: Boolean? = null,
        generatePreview: Boolean? = null,
        awaitPromise: Boolean? = null,
    ): RunScriptReturn {
        val parameter = RunScriptParameter(
            scriptId = scriptId,
            executionContextId = executionContextId,
            objectGroup = objectGroup,
            silent = silent,
            includeCommandLineAPI = includeCommandLineAPI,
            returnByValue = returnByValue,
            generatePreview = generatePreview,
            awaitPromise = awaitPromise
        )
        return runScript(parameter)
    }

    /**
     * Enables or disables async call stacks tracking.
     */
    public suspend fun setAsyncCallStackDepth(args: SetAsyncCallStackDepthParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.setAsyncCallStackDepth", parameter)
    }

    /**
     * Enables or disables async call stacks tracking.
     */
    public suspend fun setAsyncCallStackDepth(maxDepth: Int) {
        val parameter = SetAsyncCallStackDepthParameter(maxDepth = maxDepth)
        setAsyncCallStackDepth(parameter)
    }

    public suspend fun setCustomObjectFormatterEnabled(args: SetCustomObjectFormatterEnabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.setCustomObjectFormatterEnabled", parameter)
    }

    public suspend fun setCustomObjectFormatterEnabled(enabled: Boolean) {
        val parameter = SetCustomObjectFormatterEnabledParameter(enabled = enabled)
        setCustomObjectFormatterEnabled(parameter)
    }

    public suspend fun setMaxCallStackSizeToCapture(args: SetMaxCallStackSizeToCaptureParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.setMaxCallStackSizeToCapture", parameter)
    }

    public suspend fun setMaxCallStackSizeToCapture(size: Int) {
        val parameter = SetMaxCallStackSizeToCaptureParameter(size = size)
        setMaxCallStackSizeToCapture(parameter)
    }

    /**
     * Terminate current or next JavaScript execution.
     * Will cancel the termination when the outer-most script execution ends.
     */
    public suspend fun terminateExecution() {
        val parameter = null
        cdp.callCommand("Runtime.terminateExecution", parameter)
    }

    /**
     * If executionContextId is empty, adds binding with the given name on the
     * global objects of all inspected contexts, including those created later,
     * bindings survive reloads.
     * Binding function takes exactly one argument, this argument should be string,
     * in case of any other input, function throws an exception.
     * Each binding function call produces Runtime.bindingCalled notification.
     */
    public suspend fun addBinding(args: AddBindingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.addBinding", parameter)
    }

    /**
     * If executionContextId is empty, adds binding with the given name on the
     * global objects of all inspected contexts, including those created later,
     * bindings survive reloads.
     * Binding function takes exactly one argument, this argument should be string,
     * in case of any other input, function throws an exception.
     * Each binding function call produces Runtime.bindingCalled notification.
     */
    public suspend fun addBinding(
        name: String,
        executionContextId: Int? = null,
        executionContextName: String? = null,
    ) {
        val parameter = AddBindingParameter(
            name = name,
            executionContextId = executionContextId,
            executionContextName = executionContextName
        )
        addBinding(parameter)
    }

    /**
     * This method does not remove binding function from global object but
     * unsubscribes current runtime agent from Runtime.bindingCalled notifications.
     */
    public suspend fun removeBinding(args: RemoveBindingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Runtime.removeBinding", parameter)
    }

    /**
     * This method does not remove binding function from global object but
     * unsubscribes current runtime agent from Runtime.bindingCalled notifications.
     */
    public suspend fun removeBinding(name: String) {
        val parameter = RemoveBindingParameter(name = name)
        removeBinding(parameter)
    }

    /**
     * This method tries to lookup and populate exception details for a
     * JavaScript Error object.
     * Note that the stackTrace portion of the resulting exceptionDetails will
     * only be populated if the Runtime domain was enabled at the time when the
     * Error was thrown.
     */
    public suspend fun getExceptionDetails(args: GetExceptionDetailsParameter): GetExceptionDetailsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Runtime.getExceptionDetails", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * This method tries to lookup and populate exception details for a
     * JavaScript Error object.
     * Note that the stackTrace portion of the resulting exceptionDetails will
     * only be populated if the Runtime domain was enabled at the time when the
     * Error was thrown.
     */
    public suspend fun getExceptionDetails(errorObjectId: String): GetExceptionDetailsReturn {
        val parameter = GetExceptionDetailsParameter(errorObjectId = errorObjectId)
        return getExceptionDetails(parameter)
    }

    /**
     * Represents options for serialization. Overrides `generatePreview` and `returnByValue`.
     */
    @Serializable
    public data class SerializationOptions(
        public val serialization: String,
        /**
         * Deep serialization depth. Default is full depth. Respected only in `deep` serialization mode.
         */
        public val maxDepth: Int? = null,
        /**
         * Embedder-specific parameters. For example if connected to V8 in Chrome these control DOM
         * serialization via `maxNodeDepth: integer` and `includeShadowTree: "none" | "open" | "all"`.
         * Values can be only of type string or integer.
         */
        public val additionalParameters: Map<String, JsonElement>? = null,
    )

    /**
     * Represents deep serialized value.
     */
    @Serializable
    public data class DeepSerializedValue(
        public val type: String,
        public val `value`: JsonElement? = null,
        public val objectId: String? = null,
        /**
         * Set if value reference met more then once during serialization. In such
         * case, value is provided only to one of the serialized values. Unique
         * per value in the scope of one CDP call.
         */
        public val weakLocalObjectReference: Int? = null,
    )

    /**
     * Mirror object referencing original JavaScript object.
     */
    @Serializable
    public data class RemoteObject(
        /**
         * Object type.
         */
        public val type: String,
        /**
         * Object subtype hint. Specified for `object` type values only.
         * NOTE: If you change anything here, make sure to also update
         * `subtype` in `ObjectPreview` and `PropertyPreview` below.
         */
        public val subtype: String? = null,
        /**
         * Object class (constructor) name. Specified for `object` type values only.
         */
        public val className: String? = null,
        /**
         * Remote object value in case of primitive values or JSON values (if it was requested).
         */
        public val `value`: JsonElement? = null,
        /**
         * Primitive value which can not be JSON-stringified does not have `value`, but gets this
         * property.
         */
        public val unserializableValue: String? = null,
        /**
         * String representation of the object.
         */
        public val description: String? = null,
        /**
         * Deep serialized value.
         */
        public val deepSerializedValue: DeepSerializedValue? = null,
        /**
         * Unique object identifier (for non-primitive values).
         */
        public val objectId: String? = null,
        /**
         * Preview containing abbreviated property values. Specified for `object` type values only.
         */
        public val preview: ObjectPreview? = null,
        public val customPreview: CustomPreview? = null,
    )

    @Serializable
    public data class CustomPreview(
        /**
         * The JSON-stringified result of formatter.header(object, config) call.
         * It contains json ML array that represents RemoteObject.
         */
        public val `header`: String,
        /**
         * If formatter returns true as a result of formatter.hasBody call then bodyGetterId will
         * contain RemoteObjectId for the function that returns result of formatter.body(object, config) call.
         * The result value is json ML array.
         */
        public val bodyGetterId: String? = null,
    )

    /**
     * Object containing abbreviated remote object value.
     */
    @Serializable
    public data class ObjectPreview(
        /**
         * Object type.
         */
        public val type: String,
        /**
         * Object subtype hint. Specified for `object` type values only.
         */
        public val subtype: String? = null,
        /**
         * String representation of the object.
         */
        public val description: String? = null,
        /**
         * True iff some of the properties or entries of the original object did not fit.
         */
        public val overflow: Boolean,
        /**
         * List of the properties.
         */
        public val properties: List<PropertyPreview>,
        /**
         * List of the entries. Specified for `map` and `set` subtype values only.
         */
        public val entries: List<EntryPreview>? = null,
    )

    @Serializable
    public data class PropertyPreview(
        /**
         * Property name.
         */
        public val name: String,
        /**
         * Object type. Accessor means that the property itself is an accessor property.
         */
        public val type: String,
        /**
         * User-friendly property value string.
         */
        public val `value`: String? = null,
        /**
         * Nested value preview.
         */
        public val valuePreview: ObjectPreview? = null,
        /**
         * Object subtype hint. Specified for `object` type values only.
         */
        public val subtype: String? = null,
    )

    @Serializable
    public data class EntryPreview(
        /**
         * Preview of the key. Specified for map-like collection entries.
         */
        public val key: ObjectPreview? = null,
        /**
         * Preview of the value.
         */
        public val `value`: ObjectPreview,
    )

    /**
     * Object property descriptor.
     */
    @Serializable
    public data class PropertyDescriptor(
        /**
         * Property name or symbol description.
         */
        public val name: String,
        /**
         * The value associated with the property.
         */
        public val `value`: RemoteObject? = null,
        /**
         * True if the value associated with the property may be changed (data descriptors only).
         */
        public val writable: Boolean? = null,
        /**
         * A function which serves as a getter for the property, or `undefined` if there is no getter
         * (accessor descriptors only).
         */
        public val `get`: RemoteObject? = null,
        /**
         * A function which serves as a setter for the property, or `undefined` if there is no setter
         * (accessor descriptors only).
         */
        public val `set`: RemoteObject? = null,
        /**
         * True if the type of this property descriptor may be changed and if the property may be
         * deleted from the corresponding object.
         */
        public val configurable: Boolean,
        /**
         * True if this property shows up during enumeration of the properties on the corresponding
         * object.
         */
        public val enumerable: Boolean,
        /**
         * True if the result was thrown during the evaluation.
         */
        public val wasThrown: Boolean? = null,
        /**
         * True if the property is owned for the object.
         */
        public val isOwn: Boolean? = null,
        /**
         * Property symbol object, if the property is of the `symbol` type.
         */
        public val symbol: RemoteObject? = null,
    )

    /**
     * Object internal property descriptor. This property isn't normally visible in JavaScript code.
     */
    @Serializable
    public data class InternalPropertyDescriptor(
        /**
         * Conventional property name.
         */
        public val name: String,
        /**
         * The value associated with the property.
         */
        public val `value`: RemoteObject? = null,
    )

    /**
     * Object private field descriptor.
     */
    @Serializable
    public data class PrivatePropertyDescriptor(
        /**
         * Private property name.
         */
        public val name: String,
        /**
         * The value associated with the private property.
         */
        public val `value`: RemoteObject? = null,
        /**
         * A function which serves as a getter for the private property,
         * or `undefined` if there is no getter (accessor descriptors only).
         */
        public val `get`: RemoteObject? = null,
        /**
         * A function which serves as a setter for the private property,
         * or `undefined` if there is no setter (accessor descriptors only).
         */
        public val `set`: RemoteObject? = null,
    )

    /**
     * Represents function call argument. Either remote object id `objectId`, primitive `value`,
     * unserializable primitive value or neither of (for undefined) them should be specified.
     */
    @Serializable
    public data class CallArgument(
        /**
         * Primitive value or serializable javascript object.
         */
        public val `value`: JsonElement? = null,
        /**
         * Primitive value which can not be JSON-stringified.
         */
        public val unserializableValue: String? = null,
        /**
         * Remote object handle.
         */
        public val objectId: String? = null,
    )

    /**
     * Description of an isolated world.
     */
    @Serializable
    public data class ExecutionContextDescription(
        /**
         * Unique id of the execution context. It can be used to specify in which execution context
         * script evaluation should be performed.
         */
        public val id: Int,
        /**
         * Execution context origin.
         */
        public val origin: String,
        /**
         * Human readable name describing given context.
         */
        public val name: String,
        /**
         * A system-unique execution context identifier. Unlike the id, this is unique across
         * multiple processes, so can be reliably used to identify specific context while backend
         * performs a cross-process navigation.
         */
        public val uniqueId: String,
        /**
         * Embedder-specific auxiliary data likely matching {isDefault: boolean, type: 'default'|'isolated'|'worker', frameId: string}
         */
        public val auxData: Map<String, JsonElement>? = null,
    )

    /**
     * Detailed information about exception (or error) that was thrown during script compilation or
     * execution.
     */
    @Serializable
    public data class ExceptionDetails(
        /**
         * Exception id.
         */
        public val exceptionId: Int,
        /**
         * Exception text, which should be used together with exception object when available.
         */
        public val text: String,
        /**
         * Line number of the exception location (0-based).
         */
        public val lineNumber: Int,
        /**
         * Column number of the exception location (0-based).
         */
        public val columnNumber: Int,
        /**
         * Script ID of the exception location.
         */
        public val scriptId: String? = null,
        /**
         * URL of the exception location, to be used when the script was not reported.
         */
        public val url: String? = null,
        /**
         * JavaScript stack trace if available.
         */
        public val stackTrace: StackTrace? = null,
        /**
         * Exception object if available.
         */
        public val exception: RemoteObject? = null,
        /**
         * Identifier of the context where exception happened.
         */
        public val executionContextId: Int? = null,
        /**
         * Dictionary with entries of meta data that the client associated
         * with this exception, such as information about associated network
         * requests, etc.
         */
        public val exceptionMetaData: Map<String, JsonElement>? = null,
    )

    /**
     * Stack entry for runtime errors and assertions.
     */
    @Serializable
    public data class CallFrame(
        /**
         * JavaScript function name.
         */
        public val functionName: String,
        /**
         * JavaScript script id.
         */
        public val scriptId: String,
        /**
         * JavaScript script name or url.
         */
        public val url: String,
        /**
         * JavaScript script line number (0-based).
         */
        public val lineNumber: Int,
        /**
         * JavaScript script column number (0-based).
         */
        public val columnNumber: Int,
    )

    /**
     * Call frames for assertions or error messages.
     */
    @Serializable
    public data class StackTrace(
        /**
         * String label of this stack trace. For async traces this may be a name of the function that
         * initiated the async call.
         */
        public val description: String? = null,
        /**
         * JavaScript function name.
         */
        public val callFrames: List<CallFrame>,
        /**
         * Asynchronous JavaScript stack trace that preceded this stack, if available.
         */
        public val parent: StackTrace? = null,
        /**
         * Asynchronous JavaScript stack trace that preceded this stack, if available.
         */
        public val parentId: StackTraceId? = null,
    )

    /**
     * If `debuggerId` is set stack trace comes from another debugger and can be resolved there. This
     * allows to track cross-debugger calls. See `Runtime.StackTrace` and `Debugger.paused` for usages.
     */
    @Serializable
    public data class StackTraceId(
        public val id: String,
        public val debuggerId: String? = null,
    )

    /**
     * Notification is issued every time when binding is called.
     */
    @Serializable
    public data class BindingCalledParameter(
        public val name: String,
        public val payload: String,
        /**
         * Identifier of the context where the call was made.
         */
        public val executionContextId: Int,
    )

    /**
     * Issued when console API was called.
     */
    @Serializable
    public data class ConsoleAPICalledParameter(
        /**
         * Type of the call.
         */
        public val type: String,
        /**
         * Call arguments.
         */
        public val args: List<RemoteObject>,
        /**
         * Identifier of the context where the call was made.
         */
        public val executionContextId: Int,
        /**
         * Call timestamp.
         */
        public val timestamp: Double,
        /**
         * Stack trace captured when the call was made. The async stack chain is automatically reported for
         * the following call types: `assert`, `error`, `trace`, `warning`. For other types the async call
         * chain can be retrieved using `Debugger.getStackTrace` and `stackTrace.parentId` field.
         */
        public val stackTrace: StackTrace? = null,
        /**
         * Console context descriptor for calls on non-default console context (not console.*):
         * 'anonymous#unique-logger-id' for call on unnamed context, 'name#unique-logger-id' for call
         * on named context.
         */
        public val context: String? = null,
    )

    /**
     * Issued when unhandled exception was revoked.
     */
    @Serializable
    public data class ExceptionRevokedParameter(
        /**
         * Reason describing why exception was revoked.
         */
        public val reason: String,
        /**
         * The id of revoked exception, as reported in `exceptionThrown`.
         */
        public val exceptionId: Int,
    )

    /**
     * Issued when exception was thrown and unhandled.
     */
    @Serializable
    public data class ExceptionThrownParameter(
        /**
         * Timestamp of the exception.
         */
        public val timestamp: Double,
        public val exceptionDetails: ExceptionDetails,
    )

    /**
     * Issued when new execution context is created.
     */
    @Serializable
    public data class ExecutionContextCreatedParameter(
        /**
         * A newly created execution context.
         */
        public val context: ExecutionContextDescription,
    )

    /**
     * Issued when execution context is destroyed.
     */
    @Serializable
    public data class ExecutionContextDestroyedParameter(
        /**
         * Id of the destroyed context
         */
        public val executionContextId: Int,
        /**
         * Unique Id of the destroyed context
         */
        public val executionContextUniqueId: String,
    )

    /**
     * Issued when object should be inspected (for example, as a result of inspect() command line API
     * call).
     */
    @Serializable
    public data class InspectRequestedParameter(
        public val `object`: RemoteObject,
        public val hints: Map<String, JsonElement>,
        /**
         * Identifier of the context where the call was made.
         */
        public val executionContextId: Int? = null,
    )

    @Serializable
    public data class AwaitPromiseParameter(
        /**
         * Identifier of the promise.
         */
        public val promiseObjectId: String,
        /**
         * Whether the result is expected to be a JSON object that should be sent by value.
         */
        public val returnByValue: Boolean? = null,
        /**
         * Whether preview should be generated for the result.
         */
        public val generatePreview: Boolean? = null,
    )

    @Serializable
    public data class AwaitPromiseReturn(
        /**
         * Promise result. Will contain rejected value if promise was rejected.
         */
        public val result: RemoteObject,
        /**
         * Exception details if stack strace is available.
         */
        public val exceptionDetails: ExceptionDetails?,
    )

    @Serializable
    public data class CallFunctionOnParameter(
        /**
         * Declaration of the function to call.
         */
        public val functionDeclaration: String,
        /**
         * Identifier of the object to call function on. Either objectId or executionContextId should
         * be specified.
         */
        public val objectId: String? = null,
        /**
         * Call arguments. All call arguments must belong to the same JavaScript world as the target
         * object.
         */
        public val arguments: List<CallArgument>? = null,
        /**
         * In silent mode exceptions thrown during evaluation are not reported and do not pause
         * execution. Overrides `setPauseOnException` state.
         */
        public val silent: Boolean? = null,
        /**
         * Whether the result is expected to be a JSON object which should be sent by value.
         * Can be overriden by `serializationOptions`.
         */
        public val returnByValue: Boolean? = null,
        /**
         * Whether preview should be generated for the result.
         */
        public val generatePreview: Boolean? = null,
        /**
         * Whether execution should be treated as initiated by user in the UI.
         */
        public val userGesture: Boolean? = null,
        /**
         * Whether execution should `await` for resulting value and return once awaited promise is
         * resolved.
         */
        public val awaitPromise: Boolean? = null,
        /**
         * Specifies execution context which global object will be used to call function on. Either
         * executionContextId or objectId should be specified.
         */
        public val executionContextId: Int? = null,
        /**
         * Symbolic group name that can be used to release multiple objects. If objectGroup is not
         * specified and objectId is, objectGroup will be inherited from object.
         */
        public val objectGroup: String? = null,
        /**
         * Whether to throw an exception if side effect cannot be ruled out during evaluation.
         */
        public val throwOnSideEffect: Boolean? = null,
        /**
         * An alternative way to specify the execution context to call function on.
         * Compared to contextId that may be reused across processes, this is guaranteed to be
         * system-unique, so it can be used to prevent accidental function call
         * in context different than intended (e.g. as a result of navigation across process
         * boundaries).
         * This is mutually exclusive with `executionContextId`.
         */
        public val uniqueContextId: String? = null,
        /**
         * Specifies the result serialization. If provided, overrides
         * `generatePreview` and `returnByValue`.
         */
        public val serializationOptions: SerializationOptions? = null,
    )

    @Serializable
    public data class CallFunctionOnReturn(
        /**
         * Call result.
         */
        public val result: RemoteObject,
        /**
         * Exception details.
         */
        public val exceptionDetails: ExceptionDetails?,
    )

    @Serializable
    public data class CompileScriptParameter(
        /**
         * Expression to compile.
         */
        public val expression: String,
        /**
         * Source url to be set for the script.
         */
        public val sourceURL: String,
        /**
         * Specifies whether the compiled script should be persisted.
         */
        public val persistScript: Boolean,
        /**
         * Specifies in which execution context to perform script run. If the parameter is omitted the
         * evaluation will be performed in the context of the inspected page.
         */
        public val executionContextId: Int? = null,
    )

    @Serializable
    public data class CompileScriptReturn(
        /**
         * Id of the script.
         */
        public val scriptId: String?,
        /**
         * Exception details.
         */
        public val exceptionDetails: ExceptionDetails?,
    )

    @Serializable
    public data class EvaluateParameter(
        /**
         * Expression to evaluate.
         */
        public val expression: String,
        /**
         * Symbolic group name that can be used to release multiple objects.
         */
        public val objectGroup: String? = null,
        /**
         * Determines whether Command Line API should be available during the evaluation.
         */
        public val includeCommandLineAPI: Boolean? = null,
        /**
         * In silent mode exceptions thrown during evaluation are not reported and do not pause
         * execution. Overrides `setPauseOnException` state.
         */
        public val silent: Boolean? = null,
        /**
         * Specifies in which execution context to perform evaluation. If the parameter is omitted the
         * evaluation will be performed in the context of the inspected page.
         * This is mutually exclusive with `uniqueContextId`, which offers an
         * alternative way to identify the execution context that is more reliable
         * in a multi-process environment.
         */
        public val contextId: Int? = null,
        /**
         * Whether the result is expected to be a JSON object that should be sent by value.
         */
        public val returnByValue: Boolean? = null,
        /**
         * Whether preview should be generated for the result.
         */
        public val generatePreview: Boolean? = null,
        /**
         * Whether execution should be treated as initiated by user in the UI.
         */
        public val userGesture: Boolean? = null,
        /**
         * Whether execution should `await` for resulting value and return once awaited promise is
         * resolved.
         */
        public val awaitPromise: Boolean? = null,
        /**
         * Whether to throw an exception if side effect cannot be ruled out during evaluation.
         * This implies `disableBreaks` below.
         */
        public val throwOnSideEffect: Boolean? = null,
        /**
         * Terminate execution after timing out (number of milliseconds).
         */
        public val timeout: Double? = null,
        /**
         * Disable breakpoints during execution.
         */
        public val disableBreaks: Boolean? = null,
        /**
         * Setting this flag to true enables `let` re-declaration and top-level `await`.
         * Note that `let` variables can only be re-declared if they originate from
         * `replMode` themselves.
         */
        public val replMode: Boolean? = null,
        /**
         * The Content Security Policy (CSP) for the target might block 'unsafe-eval'
         * which includes eval(), Function(), setTimeout() and setInterval()
         * when called with non-callable arguments. This flag bypasses CSP for this
         * evaluation and allows unsafe-eval. Defaults to true.
         */
        public val allowUnsafeEvalBlockedByCSP: Boolean? = null,
        /**
         * An alternative way to specify the execution context to evaluate in.
         * Compared to contextId that may be reused across processes, this is guaranteed to be
         * system-unique, so it can be used to prevent accidental evaluation of the expression
         * in context different than intended (e.g. as a result of navigation across process
         * boundaries).
         * This is mutually exclusive with `contextId`.
         */
        public val uniqueContextId: String? = null,
        /**
         * Specifies the result serialization. If provided, overrides
         * `generatePreview` and `returnByValue`.
         */
        public val serializationOptions: SerializationOptions? = null,
    )

    @Serializable
    public data class EvaluateReturn(
        /**
         * Evaluation result.
         */
        public val result: RemoteObject,
        /**
         * Exception details.
         */
        public val exceptionDetails: ExceptionDetails?,
    )

    @Serializable
    public data class GetIsolateIdReturn(
        /**
         * The isolate id.
         */
        public val id: String,
    )

    @Serializable
    public data class GetHeapUsageReturn(
        /**
         * Used heap size in bytes.
         */
        public val usedSize: Double,
        /**
         * Allocated heap size in bytes.
         */
        public val totalSize: Double,
    )

    @Serializable
    public data class GetPropertiesParameter(
        /**
         * Identifier of the object to return properties for.
         */
        public val objectId: String,
        /**
         * If true, returns properties belonging only to the element itself, not to its prototype
         * chain.
         */
        public val ownProperties: Boolean? = null,
        /**
         * If true, returns accessor properties (with getter/setter) only; internal properties are not
         * returned either.
         */
        public val accessorPropertiesOnly: Boolean? = null,
        /**
         * Whether preview should be generated for the results.
         */
        public val generatePreview: Boolean? = null,
        /**
         * If true, returns non-indexed properties only.
         */
        public val nonIndexedPropertiesOnly: Boolean? = null,
    )

    @Serializable
    public data class GetPropertiesReturn(
        /**
         * Object properties.
         */
        public val result: List<PropertyDescriptor>,
        /**
         * Internal object properties (only of the element itself).
         */
        public val internalProperties: List<InternalPropertyDescriptor>?,
        /**
         * Object private properties.
         */
        public val privateProperties: List<PrivatePropertyDescriptor>?,
        /**
         * Exception details.
         */
        public val exceptionDetails: ExceptionDetails?,
    )

    @Serializable
    public data class GlobalLexicalScopeNamesParameter(
        /**
         * Specifies in which execution context to lookup global scope variables.
         */
        public val executionContextId: Int? = null,
    )

    @Serializable
    public data class GlobalLexicalScopeNamesReturn(
        public val names: List<String>,
    )

    @Serializable
    public data class QueryObjectsParameter(
        /**
         * Identifier of the prototype to return objects for.
         */
        public val prototypeObjectId: String,
        /**
         * Symbolic group name that can be used to release the results.
         */
        public val objectGroup: String? = null,
    )

    @Serializable
    public data class QueryObjectsReturn(
        /**
         * Array with objects.
         */
        public val objects: RemoteObject,
    )

    @Serializable
    public data class ReleaseObjectParameter(
        /**
         * Identifier of the object to release.
         */
        public val objectId: String,
    )

    @Serializable
    public data class ReleaseObjectGroupParameter(
        /**
         * Symbolic object group name.
         */
        public val objectGroup: String,
    )

    @Serializable
    public data class RunScriptParameter(
        /**
         * Id of the script to run.
         */
        public val scriptId: String,
        /**
         * Specifies in which execution context to perform script run. If the parameter is omitted the
         * evaluation will be performed in the context of the inspected page.
         */
        public val executionContextId: Int? = null,
        /**
         * Symbolic group name that can be used to release multiple objects.
         */
        public val objectGroup: String? = null,
        /**
         * In silent mode exceptions thrown during evaluation are not reported and do not pause
         * execution. Overrides `setPauseOnException` state.
         */
        public val silent: Boolean? = null,
        /**
         * Determines whether Command Line API should be available during the evaluation.
         */
        public val includeCommandLineAPI: Boolean? = null,
        /**
         * Whether the result is expected to be a JSON object which should be sent by value.
         */
        public val returnByValue: Boolean? = null,
        /**
         * Whether preview should be generated for the result.
         */
        public val generatePreview: Boolean? = null,
        /**
         * Whether execution should `await` for resulting value and return once awaited promise is
         * resolved.
         */
        public val awaitPromise: Boolean? = null,
    )

    @Serializable
    public data class RunScriptReturn(
        /**
         * Run result.
         */
        public val result: RemoteObject,
        /**
         * Exception details.
         */
        public val exceptionDetails: ExceptionDetails?,
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
    public data class SetCustomObjectFormatterEnabledParameter(
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetMaxCallStackSizeToCaptureParameter(
        public val size: Int,
    )

    @Serializable
    public data class AddBindingParameter(
        public val name: String,
        /**
         * If specified, the binding would only be exposed to the specified
         * execution context. If omitted and `executionContextName` is not set,
         * the binding is exposed to all execution contexts of the target.
         * This parameter is mutually exclusive with `executionContextName`.
         * Deprecated in favor of `executionContextName` due to an unclear use case
         * and bugs in implementation (crbug.com/1169639). `executionContextId` will be
         * removed in the future.
         */
        public val executionContextId: Int? = null,
        /**
         * If specified, the binding is exposed to the executionContext with
         * matching name, even for contexts created after the binding is added.
         * See also `ExecutionContext.name` and `worldName` parameter to
         * `Page.addScriptToEvaluateOnNewDocument`.
         * This parameter is mutually exclusive with `executionContextId`.
         */
        public val executionContextName: String? = null,
    )

    @Serializable
    public data class RemoveBindingParameter(
        public val name: String,
    )

    @Serializable
    public data class GetExceptionDetailsParameter(
        /**
         * The error object for which to resolve the exception details.
         */
        public val errorObjectId: String,
    )

    @Serializable
    public data class GetExceptionDetailsReturn(
        public val exceptionDetails: ExceptionDetails?,
    )
}
