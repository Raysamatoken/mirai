@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.network.protocol.tim.packet.login.requireSuccess
import net.mamoe.mirai.network.session
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.internal.PositiveNumbers
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmOverloads

/*
 * 在 [Bot] 中的方法的捷径
 */

//Contacts
suspend inline fun Bot.getQQ(@PositiveNumbers number: Long): QQ = this.contacts.getQQ(number.coerceAtLeastOrFail(0).toUInt())

suspend inline fun Bot.getQQ(number: UInt): QQ = this.contacts.getQQ(number)

suspend inline fun Bot.getGroup(id: UInt): Group = this.contacts.getGroup(GroupId(id))
suspend inline fun Bot.getGroup(@PositiveNumbers id: Long): Group = this.contacts.getGroup(GroupId(id.coerceAtLeastOrFail(0).toUInt()))
suspend inline fun Bot.getGroup(id: GroupId): Group = this.contacts.getGroup(id)
suspend inline fun Bot.getGroup(internalId: GroupInternalId): Group = this.contacts.getGroup(internalId)

/**
 * 取得机器人的群成员列表
 */
inline val Bot.groups: ContactList<Group>
    get() = this.contacts.groups

/**
 * 取得机器人的好友列表
 */
inline val Bot.qqs: ContactList<QQ>
    get() = this.contacts.qqs

/**
 * 以 [BotSession] 作为接收器 (receiver) 并调用 [block], 返回 [block] 的返回值.
 * 这个方法将能帮助使用在 [BotSession] 中定义的一些扩展方法, 如 [BotSession.sendAndExpectAsync]
 */
@UseExperimental(ExperimentalContracts::class)
inline fun <R> Bot.withSession(block: BotSession.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return with(this.network.session) { block() }
}

/**
 * 发送数据包
 * @throws IllegalStateException 当 [BotNetworkHandler.socket] 未开启时
 */
suspend inline fun Bot.sendPacket(packet: OutgoingPacket) = this.network.sendPacket(packet)

/**
 * 使用在默认配置基础上修改的配置进行登录
 */
@UseExperimental(ExperimentalContracts::class)
suspend inline fun Bot.login(noinline configuration: BotConfiguration.() -> Unit): LoginResult {
    contract {
        callsInPlace(configuration, InvocationKind.EXACTLY_ONCE)
    }
    return this.network.login(BotConfiguration().apply(configuration))
}

/**
 * 使用默认的配置 ([BotConfiguration.Default]) 登录, 返回登录结果
 */
suspend inline fun Bot.login(): LoginResult = this.network.login(BotConfiguration.Default)

/**
 * 使用默认的配置 ([BotConfiguration.Default]) 登录, 返回 [this]
 */
suspend inline fun Bot.alsoLogin(): Bot = apply { login().requireSuccess() }

/**
 * 使用默认的配置 ([BotConfiguration.Default]) 登录, 返回 [this]
 */
@UseExperimental(ExperimentalContracts::class)
suspend inline fun Bot.alsoLogin(lazyMessageWhenLoginFailed: (LoginResult) -> String): Bot {
    contract {
        callsInPlace(lazyMessageWhenLoginFailed, InvocationKind.AT_MOST_ONCE)
    }
    return this.apply {
        login().requireSuccess(lazyMessageWhenLoginFailed)
    }
}

/**
 * 添加好友
 */
@UseExperimental(ExperimentalContracts::class)
@JvmOverloads
suspend inline fun Bot.addFriend(id: UInt, lazyMessage: () -> String = { "" }, noinline lazyRemark: () -> String = { "" }): AddFriendResult {
    contract {
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }
    return this.contacts.addFriend(id, lazyMessage, lazyRemark)
}

/**
 * 取得机器人的 QQ 号
 */
inline val Bot.qqAccount: UInt get() = this.account.id