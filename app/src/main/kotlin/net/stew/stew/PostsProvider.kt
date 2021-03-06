package net.stew.stew

import android.os.AsyncTask
import org.jsoup.nodes.Document

class PostsProvider(
        private val application: Application,
        private val collection: PostCollection,
        private val listener: PostsProvider.Listener) {

    public interface Listener {
        fun onPostsLoaded(posts: Collection<Post>)
        fun onPostsLoadError(responseStatus: ResponseStatus)
    }

    private var loadingTask: AsyncTask<Void, Void, Pair<ResponseStatus, Document?>>? = null
    private var loadingTaskIsForMorePosts: Boolean = false

    protected val errorListener: (ResponseStatus) -> Unit = {
        loadingTask = null
        listener.onPostsLoadError(it)
    }
    protected val successListener: (Collection<Post>) -> Unit = {
        loadingTask = null
        listener.onPostsLoaded(it)
    }

    public fun loadPosts(lastPost: Post?) {
        if (loadingTask != null) {
            if (loadingTaskIsForMorePosts == (lastPost != null)) {
                return
            } else {
                loadingTask!!.cancel(true)
            }
        }

        loadingTask = application.api.fetchPosts(collection, lastPost, errorListener, successListener)
        loadingTaskIsForMorePosts = lastPost != null
    }

    fun isLoading(): Boolean = loadingTask != null

}
