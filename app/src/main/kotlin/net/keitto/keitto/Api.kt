package net.keitto.keitto

import android.net.Uri
import android.os.AsyncTask
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.text.Regex

class Api(val application: Application) {

    fun logIn(userName: String, password: String, listener: (String, String, String) -> Unit) {
        val loginPageConnection = connect("/login", true)
        executeRequest(loginPageConnection) {
            val sessionId = loginPageConnection.response().cookie("soup_session_id")
            val authenticityToken = it.select("input[name=authenticity_token]").attr("value")

            val connection = connect("/login", true).
                method(Connection.Method.POST).
                cookie("soup_session_id", sessionId).
                data("login", userName).
                data("password", password).
                data("authenticity_token", authenticityToken)

            executeRequest(connection) {
                val userIdCookie = connection.response().cookie("soup_user_id")
                val sessionIdCookie = connection.response().cookie("soup_session_id")
                val csrfToken = it.select("meta[name=csrf-token]").attr("content")
                listener(userIdCookie, sessionIdCookie, csrfToken)
            }
        }
    }

    fun fetchPosts(listener: (Collection<Post>) -> Unit) {
        val connection = connect("/friends")

        executeRequest(connection) {
            val posts = it.select(".post_image").
                map {
                    val id = it.attr("id").replace(Regex("[^0-9]"), "").toInt()
                    val src = it.select(".imagecontainer img").attr("src")
                    Post(id, Uri.parse(src))
                }
            listener(posts)
        }
    }

    fun repost(post: Post) {
        val connection = connect("/remote/repost").
            method(Connection.Method.POST).
            data("parent_id", post.id.toString())
        executeRequest(connection, null)
    }

    private fun connect(path: String, useSsl: Boolean = false): Connection {
        val connection = Jsoup.connect("http${if (useSsl) "s" else ""}://www.soup.io${path}")
        val currentSession = application.currentSession

        if (currentSession != null) {
            connection.cookie("soup_user_id", currentSession.userIdCookie).
            cookie("soup_session_id", currentSession.sessionIdCookie).
            header("X-CSRF-Token", currentSession.csrfToken)
        }

        return connection
    }

    private fun executeRequest(connection: Connection, listener: ((Document) -> Unit)?) {
        val task = object: AsyncTask<Void, Void, Document>() {
            override fun doInBackground(vararg params: Void?): Document {
                return connection.execute().parse()
            }

            override fun onPostExecute(document: Document) {
                if (listener != null) {
                    listener(document)
                }
            }
        }
        task.execute()
    }

}
