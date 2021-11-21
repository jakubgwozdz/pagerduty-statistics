import WeekDays.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.datetime.Clock.System
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.dom.removeClass
import kotlinx.html.Entities.nbsp
import kotlinx.html.InputType.checkBox
import kotlinx.html.InputType.dateTimeLocal
import kotlinx.html.TagConsumer
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.hidden
import kotlinx.html.id
import kotlinx.html.js.a
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.h3
import kotlinx.html.js.h6
import kotlinx.html.js.i
import kotlinx.html.js.input
import kotlinx.html.js.label
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.p
import kotlinx.html.js.section
import kotlinx.html.js.small
import kotlinx.html.js.span
import kotlinx.html.js.table
import kotlinx.html.js.tbody
import kotlinx.html.js.td
import kotlinx.html.js.th
import kotlinx.html.js.thead
import kotlinx.html.js.tr
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import pdstats.EOB
import pdstats.SOB
import pdstats.Schedule
import pdstats.User
import pdstats.containsUser
import pdstats.prevMonth
import pdstats.prevYear
import pdstats.thisMonthSoFar
import pdstats.thisYearSoFar

@JsExport
fun createUI() {
    document.body!!.append {
        createHeader()
        createMainContent()
    }
}

const val currentUserParagraphId = "welcome-user"
const val schedulesDivId = "schedules"
const val showOnlyMineCheckboxId = "show-only-mine"
const val schedulesFormDivId = "shedules-form"
const val legendRowId = "legend-row"
const val loadingDivId = "loading-div"
const val welcomeDivId = "welcome-div"
const val anonymizeCheckboxId = "anonymize"
const val showSinceInputId = "show-since"
const val showUntilInputId = "show-until"

private fun TagConsumer<HTMLElement>.createMainContent() = section("section") {
    div("hero") {
        div("hero-body") {
            div("content") {
                loginTile.createLoginTile(this@createMainContent)
                createWelcomeTile()
                div("space")
                createSchedulesForm()
                createLegend()
                div {
                    id = schedulesDivId
                }
            }
        }
    }
}

private fun TagConsumer<HTMLElement>.createSchedulesForm() = div("u-none") {
    id = schedulesFormDivId
    var showSince: HTMLInputElement? = null
    var showUntil: HTMLInputElement? = null
    div("row") {
        div("col-3") {
            p("m-0") { +"Show since: " }
            showSince = input(dateTimeLocal, name = showSinceInputId, classes = "input-small") {
                id = showSinceInputId
                value = since.toLocalDateTime(timeZone).toString()
                onChangeFunction = { event ->
                    since = showSince!!.value
                        .also { localStorage.setItem("defaultSince", it) }
                        .toLocalDateTime().toInstant(timeZone)
                }
            }
        }
        div("col-3") {
            p("m-0") { +"Show until: " }
            showUntil = input(dateTimeLocal, name = showUntilInputId, classes = "input-small") {
                id = showUntilInputId
                value = until.toLocalDateTime(timeZone).toString()
                onChangeFunction = { event ->
                    until = showUntil!!.value
                        .toLocalDateTime().toInstant(timeZone)
                }
            }
        }
        div("col-6") {
            p("m-0") { +"Presets: " }
            div("btn-group") {
                button(classes = "btn-small") {
                    +"Prev."
                    +nbsp
                    +"Year"
                    onClickFunction = {
                        val range = System.now().prevYear(timeZone)
                        showSince?.value = range.start.toString()
                        showUntil?.value = range.endInclusive.toString()
                        showSince?.onchange?.invoke(it)
                        showUntil?.onchange?.invoke(it) as Unit
                    }
                }
                button(classes = "btn-small") {
                    +"This"
                    +nbsp
                    +"Year"
                    onClickFunction = {
                        val range = System.now().thisYearSoFar(timeZone)
                        showSince?.value = range.start.toString()
                        showUntil?.value = range.endInclusive.toString()
                        showSince?.onchange?.invoke(it)
                        showUntil?.onchange?.invoke(it) as Unit
                    }
                }
                button(classes = "btn-small") {
                    +"Prev."
                    +nbsp
                    +"Month"
                    onClickFunction = {
                        val range = System.now().prevMonth(timeZone)
                        showSince?.value = range.start.toString()
                        showUntil?.value = range.endInclusive.toString()
                        showSince?.onchange?.invoke(it)
                        showUntil?.onchange?.invoke(it) as Unit
                    }
                }
                button(classes = "btn-small") {
                    +"This"
                    +nbsp
                    +"Month"
                    onClickFunction = {
                        val range = System.now().thisMonthSoFar(timeZone)
                        showSince?.value = range.start.toString()
                        showUntil?.value = range.endInclusive.toString()
                        showSince?.onchange?.invoke(it)
                        showUntil?.onchange?.invoke(it) as Unit
                    }
                }
            }
        }
    }
    div("row") {
        div("col-5") {
            div("form-ext-control") {
                label("form-ext-toggle__label") {
                    span("pr-1") { +"Show only my schedules" }
                    div("form-ext-toggle") {
                        input(type = checkBox, classes = "form-ext-input", name = showOnlyMineCheckboxId) {
                            checked = true
                            id = showOnlyMineCheckboxId
                            onClickFunction = { showOnlyMineChanged() }
                        }
                        div("form-ext-toggle__toggler") { i {} }
                    }
                }
            }
        }
    }
}

private fun TagConsumer<HTMLElement>.createLegend() {
    div("row") {
        id = legendRowId
        hidden = true
        div("col") {
            p("m-0") { +"Legend: " }
            table {
                style = "width: 100%;"
                thead {
                    tr {
                        WeekDays.values().forEach {
                            th {
                                colSpan = when (it) {
                                    Sat, Sun -> "1"
                                    else -> "3"
                                }
                                classes = classes + when (it) {
                                    Sat, Sun -> "bg-gray-100 text-red-400"
                                    else -> "bg-gray-100 text-gray-800"
                                }
                                small { +it.name }
                            }
                        }
                    }
                }
                tbody {
                    tr {
                        WeekDays.values().forEach {
                            when (it) {
                                Sat, Sun -> {
                                    td("u-text-center bg-red-400") {
                                        +nbsp
                                        style = "font-size:0.5rem; width: ${100.0 / 7.0}%"
                                    }
                                }
                                else -> {
                                    td("u-text-center") {
                                        style = "font-size:0.5rem; width: ${100.0 / 7.0 / 24.0 * 9.0}%"
                                        +"0-$SOB"
                                        classes = classes + when (it) {
                                            Mon -> "bg-gray-800 text-gray-100"
                                            else -> "bg-gray-800 text-gray-100"
                                        }
                                    }
                                    td("u-text-center") {
                                        style = "font-size:0.5rem; width: ${100.0 / 7.0 / 24.0 * 8.0}%"
                                        +"$SOB-$EOB"
                                        classes = classes + "text-gray-800 bg-gray-100"
                                    }
                                    td("u-text-center") {
                                        style = "font-size:0.5rem; width: ${100.0 / 7.0 / 24.0 * 7.0}%"
                                        +"$EOB-24"
                                        classes = classes + when (it) {
                                            Fri -> "bg-gray-800 text-gray-100"
                                            else -> "bg-gray-800 text-gray-100"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TagConsumer<HTMLElement>.createWelcomeTile() {
    div("tile u-items-center u-none") {
        id = welcomeDivId
        div("tile__icon") {
            h3 {
                +"👋👋"
            }
        }
        div("tile__container") {
            p("tile__title m-0") {
                id = currentUserParagraphId
                +"..."
            }
        }
    }
    div("tile u-items-center u-none") {
        id = loadingDivId
        div("tile__icon animated loading hide-text") {
            p { +"Hidden" }
        }
        div("tile__container") {
            p("tile__title m-0") {
                +"Loading schedules"
            }
        }
    }
}


enum class WeekDays { Mon, Tue, Wed, Thu, Fri, Sat, Sun }

private fun TagConsumer<HTMLElement>.createHeader() {
    div("header header-fixed u-unselectable header-animated") {
        div("header-brand") {
            div("nav-item no-hover") {
                h6("title") { +"PagerDuty Statistics" }
            }
            div("nav-item nav-btn") {
                id = "header-btn"
                span { }
                span { }
                span { }
                onClickFunction = { _ ->
                    document.getElementById("header-btn")?.classList?.toggle("active")
                    document.getElementById("header-menu")?.classList?.toggle("active")
                }
            }
        }
        div("header-nav") {
            id = "header-menu"
            div("nav-left") {
                div("nav-item text-center") {
                    a(href = "https://github.com/jakubgwozdz/pagerduty-statistics", target = "_blank") {
                        span("icon") {
                            i("fab fa-wrapper fa-github")
                        }
                    }
                }
            }
            div("nav-right") {
                div("nav-item u-justify-flex-end") {
                    div("form-ext-control") {
                        label("form-ext-toggle__label") {
                            span("pr-1") { +"Anonymize" }
                            div("form-ext-toggle") {
                                input(checkBox, classes = "form-ext-input", name = anonymizeCheckboxId) {
                                    checked = false
                                    id = anonymizeCheckboxId
                                }
                                div("form-ext-toggle__toggler") { i {} }
                            }
                        }
                    }
                }
            }
        }
    }
}

val scheduleFrames = mutableMapOf<String, ScheduleFrame>()

fun updateScheduleList(schedules: List<Schedule>) {
    val schedulesDiv = document.byId<HTMLDivElement>(schedulesDivId)
    val showOnlyMine = document.byId<HTMLInputElement>(showOnlyMineCheckboxId)
    schedulesDiv.innerHTML = ""
    scheduleFrames.clear()
    schedulesDiv.append {
        schedules.forEach { schedule ->
            val mine = schedule.containsUser(currentUser!!)
            val frame = ScheduleFrame(schedule, mine)
            val hidden = !mine && showOnlyMine.checked
            frame.createFrame(this, hidden)
            scheduleFrames[schedule.id] = frame
        }
    }
}


fun updateCurrentUser(header: HTMLParagraphElement, user: User?) {
    header.textContent = "Hello there, ${user?.jobTitle ?: ""} ${user?.summary}"
    loginTile.markSuccess()
    document.byId<HTMLDivElement>(welcomeDivId).removeClass("u-none")
}

