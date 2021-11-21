import kotlinx.browser.document
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.Entities.nbsp
import kotlinx.html.TagConsumer
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.hidden
import kotlinx.html.id
import kotlinx.html.js.b
import kotlinx.html.js.button
import kotlinx.html.js.code
import kotlinx.html.js.div
import kotlinx.html.js.h6
import kotlinx.html.js.i
import kotlinx.html.js.li
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
import kotlinx.html.js.ul
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import pdstats.DutyCategory
import pdstats.DutyCategory.*
import pdstats.Schedule
import pdstats.analyzeSchedule
import kotlin.time.Duration
import kotlin.time.DurationUnit.MINUTES


private fun scheduleStatsDivId(scheduleId: String) = "schedule-$scheduleId-stats"
private fun scheduleDivId(scheduleId: String) = "schedule-$scheduleId"

enum class Page { Roster, Schedule, Stats, Incidents }

class ScheduleFrame(val schedule: Schedule, val mine: Boolean) {

    fun show() {
        frameDiv.hidden = false
    }

    fun hide() {
        frameDiv.hidden = true
    }

    lateinit var frameDiv: HTMLDivElement

    private lateinit var rosterLi: HTMLLIElement
    private lateinit var scheduleLi: HTMLLIElement
    private lateinit var statsLi: HTMLLIElement
    private lateinit var incidentsLi: HTMLLIElement

    private lateinit var rosterDiv: HTMLDivElement
    private lateinit var scheduleDiv: HTMLDivElement
    private lateinit var statsDiv: HTMLDivElement
    private lateinit var incidentsDiv: HTMLDivElement

    fun createFrame(
        t: TagConsumer<HTMLElement>,
        hidden: Boolean
    ): HTMLDivElement = with(t) {
        check(!this@ScheduleFrame::frameDiv.isInitialized)
        div("frame mb-2") {
            id = scheduleDivId(schedule.id)
            this.hidden = hidden
            div("frame__header") {
                div("frame__title") {
                    if (mine) {
                        classes = classes + "text-primary"
                    }
                    +schedule.summary
                }
//                div("frame__subtitle") {
//                    div("tag-container") {
//                        id = "schedule-${schedule.id}-users"
//                        schedule.users?.forEach { user ->
//                            div("tag") {
//                                +user.summary
//                            }
//                        }
//                    }
//                }
            }
            div("frame__body") {
                div("tab-container") {
                    ul {
                        rosterLi = li("selected") {
                            div("tab-item-content") { +"Roster" }
                            onClickFunction = { select(Page.Roster) }
                        }
                        scheduleLi = li("u-disabled") {
                            div("tab-item-content") { +"Schedule" }
                            onClickFunction = { select(Page.Schedule) }
                        }
                        statsLi = li("u-disabled") {
                            div("tab-item-content") { +"Stats" }
                            onClickFunction = { select(Page.Stats) }
                        }
                        incidentsLi = li("u-disabled") {
                            div("tab-item-content") { +"Incidents" }
                            onClickFunction = { select(Page.Incidents) }
                        }
                    }
                }

                rosterDiv = div(" tag-container") {
                    schedule.users?.forEach { user ->
                        div("tag") {
                            +user.summary
                        }
                    }
                }

                scheduleDiv = div(" u-none") {
                    id = scheduleStatsDivId(schedule.id)
                }

                statsDiv = div(" u-none") {
                    id = scheduleStatsDivId(schedule.id)
                }

                incidentsDiv = div(" u-none") {
                    id = scheduleStatsDivId(schedule.id)
                }
            }

            div("frame__footer p-0") {
                button(classes = "btn-transparent m-0 u-items-center u-flex") {
                    i("fa-wrapper fas fa-sync")
                    +nbsp
                    +"Load schedule"
                    val loadingDiv = div("u-none animated loading hide-text p-0") {
                        p("m-0") { +"Hidden" }
                    }
                    onClickFunction = { fetchSchedule(it, schedule.id, loadingDiv) }
                }
            }
        }
    }.also { frameDiv = it }

    private fun modifyPageSelection(divElem: HTMLDivElement, liElem: HTMLLIElement, selected: Boolean) {
        if (selected) {
            liElem.addClass("selected")
            divElem.removeClass("u-none")
        } else {
            liElem.removeClass("selected")
            divElem.addClass("u-none")
        }
    }

    fun select(page: Page) {
        modifyPageSelection(rosterDiv, rosterLi, page == Page.Roster)
        modifyPageSelection(scheduleDiv, scheduleLi, page == Page.Schedule)
        modifyPageSelection(statsDiv, statsLi, page == Page.Stats)
        modifyPageSelection(incidentsDiv, incidentsLi, page == Page.Incidents)
    }

    private fun updateStats(scheduleYears: List<ScheduleYearModel>) {
        statsDiv.innerHTML = ""
        statsDiv.append {
            scheduleYears.forEach { schedule ->
                section {
                    scheduleYearTitleParagraph(schedule)
                    table {

                        style = "width: 100%;"

                        tbody {
                            val maxDuration = schedule.stats.usersStats.maxOf {
                                it.durations.map(Pair<DutyCategory, Duration>::second)
                                    .fold(Duration.ZERO, Duration::plus)
                            }


                            schedule.stats.usersStats.forEach { userStats ->
                                tr {
                                    th(classes = "u-text-right p-1") {
                                        style = "width: 30%;"
                                        +"${userStats.name}:"
                                    }
                                    td {
                                        div("row") {
                                            userStats.durations.forEach {
                                                val pc =
                                                    it.second.toDouble(MINUTES) * 100 / maxDuration.toDouble(
                                                        MINUTES
                                                    )
                                                div("p-1") {
                                                    +"${it.second}"
                                                    style = "width: ${pc.toInt()}%;"
                                                    classes = classes + when (it.first) {
                                                        WEEKEND -> "bg-red-400"
                                                        AFTERDUTY -> "bg-gray-800 text-gray-100"
                                                        WORKDAY -> "text-gray-800 bg-gray-100"
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
        }
        statsLi.removeClass("u-disabled")
    }

    private fun TagConsumer<HTMLElement>.scheduleYearTitleParagraph(schedule: ScheduleYearModel) = p {
        b { +"${schedule.year}:" }
        if (schedule.since > LocalDateTime(schedule.year, 1, 1, 0, 0)) {
            span { small { +" (since ${schedule.since})" } }
        }
        if (schedule.until < LocalDateTime(schedule.year + 1, 1, 1, 0, 0)) {
            span { small { +" (until ${schedule.until})" } }
        }
    }


    private fun updateSchedule(scheduleYears: List<ScheduleYearModel>) {
        scheduleDiv.innerHTML = ""
        scheduleDiv.append {
            scheduleYears.forEach { schedule ->
                scheduleYearTitleParagraph(schedule)
                table("table") {
                    thead {
                        tr {
                            th { +"User" }
                            th { +"Since" }
                            th { +"Until" }
                            th { +"Duration" }
                        }
                    }
                    tbody {
                        schedule.schedule.finalSchedule!!.entries.forEach {
                            tr {
                                td { +it.user.summary }
                                td { +it.start!!.toLocalDateTime(timeZone).toString() }
                                td { +it.end!!.toLocalDateTime(timeZone).toString() }
                                td { +(it.end!! - it.start!!).toString() }
                            }
                        }
                    }
                }
            }
        }
        scheduleLi.removeClass("u-disabled")
    }

    private fun updateIncidents(scheduleYears: List<ScheduleYearModel>) {
        incidentsDiv.innerHTML = ""
        incidentsDiv.append {
            div("placeholder") {
                div("placeholder-icon") {
                    span("icon") { i("fa fa-wrapper fa-tools x-large")}
                }
                code("placeholder-title") {+"// TODO: Implement after the hackday"}
                div("placeholder-subtitle") {+"Haha, nope, probably never, PagerDuty API is too messed up"}
            }
        }
        incidentsLi.removeClass("u-disabled")
    }

    fun updateScheduleYears(scheduleYears: List<Schedule>) {
        val models = scheduleYears.filter {
            it.finalSchedule?.entries?.isNotEmpty() ?: false
        }.map {
            ScheduleYearModel(it)
        }
        updateStats(models)
        updateSchedule(models)
        updateIncidents(models)
        select(Page.Stats)
        document.byId<HTMLElement>(legendRowId).hidden = false
    }

}

private data class ScheduleYearModel(val schedule: Schedule) {
    val firstEntry = schedule.finalSchedule!!.entries.first()
    val since = firstEntry.start!!.toLocalDateTime(timeZone)
    val until = schedule.finalSchedule!!.entries.last().end!!.toLocalDateTime(timeZone)
    val year = since.year

    val stats by lazy { analyzeSchedule(schedule) }

    val maxDuration by lazy {
        stats.usersStats.maxOf {
            it.durations.map(Pair<DutyCategory, Duration>::second)
                .fold(Duration.ZERO, Duration::plus)
        }
    }

}
