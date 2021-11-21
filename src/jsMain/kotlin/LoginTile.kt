import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.ButtonType.submit
import kotlinx.html.InputType.password
import kotlinx.html.TagConsumer
import kotlinx.html.id
import kotlinx.html.js.a
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.form
import kotlinx.html.js.h3
import kotlinx.html.js.i
import kotlinx.html.js.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.p
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

class ApiKey {
    private val inputId = "pagerduty-apikey"
    lateinit var inputElement: HTMLInputElement
    val value: String get() = inputElement.value

    fun markSuccess() {
        with(inputElement) {
            removeClass("text-danger")
            removeClass("input-error")
            addClass("text-success")
            addClass("input-success")
        }
    }

    fun markError() {
        with(inputElement) {
            removeClass("text-success")
            removeClass("input-success")
            addClass("text-danger")
            addClass("input-error")
        }
        focus()
    }

    fun focus() = inputElement.focus()

    fun createField(t: TagConsumer<HTMLElement>) = with(t) {
        inputElement = input(type = password, classes = "form-group-input input-contains-icon", name = inputId) {
            id = inputId
            placeholder = "Your personal API Key from PagerDuty"
            autoComplete = true
        }
        span("form-group-input icon") { i("fa-wrapper fas fa-key fa-2x") {} }
    }

}

val loginTile = LoginTile()

class LoginTile {
    private val loginDivId = "login-div"

    lateinit var loginDiv: HTMLElement
    val apiKey = ApiKey()

    fun markSuccess() {
        apiKey.markSuccess() // TODO: pointless atm
        loginDiv.addClass("u-none")
    }

    fun markError() {
        apiKey.markError()
        loginDiv.removeClass("u-none")
    }

    fun createLoginTile(t: TagConsumer<HTMLElement>) = with(t) {
        loginDiv = div {
            id = loginDivId
            div("tile u-items-center") {
                div("tile__icon") {
                    h3 {
                        +"👋"
                    }
                }
                div("tile__container") {
                    p("tile__title m-0") {
                        +"First, insert your APIKEY from PagerDuty"
                    }
                }
            }
            createApiKeyForm(t)
        }
    }

    private fun createApiKeyForm(t: TagConsumer<HTMLElement>) = with(t) {
        listOf(
            form {
                div("input-control form-group") {
                    apiKey.createField(t)
                    button(classes = "form-group-btn btn-primary", type = submit) {
                        id = "list-schedules-btn"
                        +"Connect"
                        onClickFunction = { listSchedules(it) }
                    }
                }
            },
            span("info u-text-center") {
                +"Check "
                a(
                    href = "https://support.pagerduty.com/docs/generating-api-keys#generating-a-personal-rest-api-key",
                    classes = "u u-LR"
                ) {
                    +"PagerDuty help pages"
                }
                +" to generate personal API Key"
            }
        )
    }

}

