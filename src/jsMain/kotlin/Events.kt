import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.events.Event
import pdstats.AnonymizingClient
import pdstats.Client
import pdstats.PDClient
import pdstats.containsUser

fun eventHandling(event: Event?, block: suspend () -> Unit) {
    GlobalScope.launch {
        try {
            block()
        } catch (e: Throwable) {
            console.log(e)
            loginTile.markError()
        }
    }
    event?.preventDefault()
}

suspend fun setupCurrentUser() {
    val header: HTMLParagraphElement = document.byId(currentUserParagraphId)
    currentUser = createPDClient().currentUser()
    updateCurrentUser(header, currentUser)
}


fun listSchedules(event: Event?) = eventHandling(event) {
    if (currentUser == null) setupCurrentUser()
    document.byId<HTMLElement>(loadingDivId).removeClass("u-none")
    schedules.clear()
    val newSchedules = createPDClient().schedules()
    document.byId<HTMLElement>(loadingDivId).addClass("u-none")
    document.byId<HTMLElement>(schedulesFormDivId).removeClass("u-none")
    schedules.addAll(newSchedules)
    updateScheduleList(schedules)
}

fun fetchSchedule(event: Event?, scheduleId: String, loadingDiv: HTMLDivElement? = null) = eventHandling(event) {
    loadingDiv?.removeClass("u-none")
    val scheduleYears = createPDClient().schedule(scheduleId, since, until)
    loadingDiv?.addClass("u-none")
    scheduleFrames[scheduleId]?.updateScheduleYears(scheduleYears)
}

fun showOnlyMineChanged() {
    val showOnlyMine: HTMLInputElement = document.byId(showOnlyMineCheckboxId)
    schedules.forEach { schedule ->
        val scheduleElem = scheduleFrames[schedule.id]
        val mySchedule = schedule.containsUser(currentUser!!)
        if (!mySchedule) {
            if (showOnlyMine.checked) scheduleElem?.hide() else scheduleElem?.show()
        }
    }
}

fun createPDClient(): Client {
    val anonymize = document.byId<HTMLInputElement>(anonymizeCheckboxId).checked
    val realClient = PDClient(loginTile.apiKey.value)
    return if (anonymize) AnonymizingClient(realClient) else realClient
}


inline fun <reified K : HTMLElement?> Document.byId(elementId: String) =
    getElementById(elementId) as K

