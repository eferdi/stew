package net.stew.stew

import android.os.AsyncTask
import org.jsoup.nodes.Document

abstract class PostsProvider(private val listener: PostsProvider.Listener, protected val application: Application) {

    public interface Listener {
        fun onPostsLoaded(posts: Collection<Post>)
        fun onPostsLoadError()
    }

    private var loadingTask: AsyncTask<Void, Void, Document?>? = null

    protected val errorListener: () -> Unit = {
        loadingTask = null
        listener.onPostsLoadError()
    }
    protected val successListener: (Collection<Post>) -> Unit = {
        loadingTask = null
        listener.onPostsLoaded(it)
    }

    public fun loadPosts(lastPost: Post?) {
        if (loadingTask != null) {
            loadingTask!!.cancel(true)
        }

        loadingTask = fetchPosts(lastPost)
    }

    fun isLoading(): Boolean = loadingTask != null

    abstract protected fun fetchPosts(lastPost: Post?): AsyncTask<Void, Void, Document?>

}
