package com.sizeofanton.roboping

import io.reactivex.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 *  Class for pinging remote host
 *
 *  This has no public available constructor, inner data class
 *  Builder is used for creating instances
 *  @property broadcastEnable Use this option to allow pinging a broadcast address
 *  @property count Use this option to to set the number of times to send the ping request
 *  @property soDebugEnable Use this option to set the SO-DEBUG option on the socket being used
 *  @property floodNetworkEnable Use this option to flood network by sending hundred or more packets per second
 *  @property interval Use this option to specify an interval between successive packet transmissions. The default value of interval is 1 second
 *  @property ttl 	Use this option to set the Time To Live (max number of hops)
 *  @property deadline Use this option to specify a timeout, in seconds, before ping exits, regardless of how many packets have been sent or received.
 *  @property timeout Use this option to set the time(seconds) to wait for a response
 *  @constructor Create an class for performing ping with requested configuration
 */
class RoboPing private constructor(
    private val broadcastEnable: Boolean = false,
    private val count: Int = 3,
    private val soDebugEnable: Boolean = false,
    private val floodNetworkEnable: Boolean = false,
    private val interval: Int = 1,
    private val ttl: Int = 50,
    private val deadline: Int = 1000,
    private val timeout: Int = 1000
) {
    /**
     * Data class for implementing the Builder pattern
     */
    data class Builder(
        private var broadcastEnable: Boolean = false,
        private var count: Int = 3,
        private var soDebugEnable: Boolean = false,
        private var floodNetworkEnable: Boolean = false,
        private var interval: Int = 1,
        private var ttl: Int = 50,
        private var deadline: Int = 1000,
        private var timeout: Int = 1000
    ) {
        fun enableBroadcast() = apply {this.broadcastEnable = true}
        fun setCount(count: Int) = apply {this.count = count}
        fun enableSoDebug() = apply {this.soDebugEnable = true}
        fun enableFlood() = apply {this.floodNetworkEnable = true}
        fun setInterval(interval: Int) = apply {this.interval = interval}
        fun setTtl(ttl: Int) = apply {this.ttl = ttl}
        fun setDeadline(deadline: Int) = apply { this.deadline = deadline }
        fun setTimeout(timeout: Int) = apply { this.timeout = timeout }
        fun build() = RoboPing(
            broadcastEnable,
            count,
            soDebugEnable,
            floodNetworkEnable,
            interval,
            ttl,
            deadline,
            timeout
        )
    }

    /**
     * Method for pinging host without additional output information
     * @param hostname DNS or IP address
     * @return Completable, that send onComplete() to subscribers if ping was successful
     *          or onError() if was not
     */
    fun ping(hostname: String): Completable {
        return object : Completable() {
            override fun subscribeActual(observer: CompletableObserver) {
                val process = buildProcess(hostname)
                process.waitFor()
                if (process.exitValue() == 0) observer.onComplete()
                else observer.onError(Throwable("Host unreachable"))
            }
        }
    }

    /**
     * Method for pinging host with capturing the stout and stderr
     * @param hostname DNS or IP address
     * @return Observable<String> that send stdout and stderr to subscribers
     */
    fun pingVerbose(hostname: String): Observable<String> {
        return Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(emitter: ObservableEmitter<String>) {
                val process = buildProcess(hostname)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val iterator = reader.lineSequence().iterator()
                while (iterator.hasNext()) {
                    val line = iterator.next()
                    emitter.onNext(line)
                }
                val exitCode = process.waitFor()
                emitter.onNext("ping finished with code - $exitCode")
                emitter.onComplete()
            }
        })
    }

    /**
     * Method creates a process, depending on the current configuration
     * @param hostname DNS or IP address
     * @return Process that runs ping command
     */
    private fun buildProcess(hostname: String): Process {
        val command = mutableListOf<String>().apply {
            add("/system/bin/ping")
            if (broadcastEnable) add("-b")
            add("-c $count")
            if (soDebugEnable) add("-d")
            if (floodNetworkEnable) add("-f")
            add("-i $interval")
            add("-t $ttl")
            add("-w $deadline")
            add("-W $timeout")
            add(hostname)
        }

        return ProcessBuilder(command).start()
    }
}
