package com.mercandalli.android.apps.files.file_column_horizontal_lists

import android.content.Context
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.mercandalli.android.apps.files.R
import com.mercandalli.android.apps.files.file_column_detail.FileColumnDetailView
import com.mercandalli.android.apps.files.file_column_list.FileColumnListView
import com.mercandalli.android.apps.files.file_column_row.FileColumnRow
import com.mercandalli.android.apps.files.main.ApplicationGraph
import com.mercandalli.sdk.files.api.File

class FileColumnHorizontalLists @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FileColumnHorizontalListsContract.Screen {

    private val userAction: FileColumnHorizontalListsContract.UserAction
    private val fileListViews = ArrayList<FileColumnListView>()
    private val fileColumnDetailView: FileColumnDetailView
    private val fab: FloatingActionButton
    private val fileListViewContainer: LinearLayout
    private val horizontalScrollView: HorizontalScrollView
    private var fileHorizontalListsSelectedFileListener: FileHorizontalListsSelectedFileListener? = null

    init {
        View.inflate(context, R.layout.view_file_column_horizontal_lists, this)
        fab = findViewById(R.id.view_file_column_horizontal_lists_fab)
        fileColumnDetailView = findViewById(R.id.view_file_column_horizontal_lists_file_detail_view)
        fileListViewContainer = findViewById(R.id.view_file_column_horizontal_lists_list_view_container)
        horizontalScrollView = findViewById(R.id.view_file_column_horizontal_lists_horizontal_scroll_view)
        userAction = createUserAction()

        val fileListView = createList(0)
        fileListViews.add(fileListView)
        fileListViewContainer.addView(fileListView)
        fab.setOnClickListener {
            userAction.onFabClicked()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        userAction.onAttached()
    }

    override fun onDetachedFromWindow() {
        userAction.onDetached()
        super.onDetachedFromWindow()
    }

    override fun createList(path: String, index: Int) {
        if (index != fileListViews.size) {
            throw IllegalStateException("Wrong index. Index:$index fileListViews.size:" +
                    fileListViews.size)
        }
        val fileListView = createList(index)
        fileListView.setPath(path)
        fileListViews.add(fileListView)
        fileListViewContainer.addView(fileListView)

        horizontalScrollView.scrollBarFadeDuration = 10_000
    }

    override fun setPath(path: String, index: Int) {
        if (index >= fileListViews.size) {
            throw IllegalStateException("Wrong index. Index:$index fileListViews.size:" +
                    fileListViews.size)
        }
        fileListViews[index].setPath(path)
    }

    override fun setListsSize(size: Int) {
        for (index in size until fileListViews.size) {
            fileListViewContainer.removeView(fileListViews[index])
        }
        val list = ArrayList(fileListViews.subList(0, size))
        fileListViews.clear()
        fileListViews.addAll(list)
    }

    override fun scrollEnd() {
        horizontalScrollView.postDelayed({
            horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }, 100L)
    }

    override fun selectPath(path: String?) {
        for (fileListView in fileListViews) {
            fileListView.onPathSelected(path)
        }
        fileHorizontalListsSelectedFileListener?.onSelectedFilePathChanged(path)
    }

    override fun showFab() {
        fab.show()
    }

    override fun hideFab() {
        fab.hide()
    }

    override fun setFabIcon(drawableRes: Int) {
        fab.setImageResource(drawableRes)
    }

    override fun showFileDetailView(file: File) {
        fileColumnDetailView.visibility = View.VISIBLE
        fileColumnDetailView.setFile(file)
    }

    override fun hideFileDetailView() {
        fileColumnDetailView.visibility = View.GONE
        fileColumnDetailView.setFile(null)
    }

    fun onResume() {
        for (fileListView in fileListViews) {
            fileListView.onResume()
        }
    }

    fun setFileHorizontalListsSelectedFileListener(listener: FileHorizontalListsSelectedFileListener?) {
        fileHorizontalListsSelectedFileListener = listener
    }

    private fun createUserAction(): FileColumnHorizontalListsContract.UserAction {
        if (isInEditMode) {
            return object : FileColumnHorizontalListsContract.UserAction {
                override fun onAttached() {}
                override fun onDetached() {}
                override fun onFileClicked(index: Int, file: File) {}
                override fun onFileLongClicked(index: Int, file: File) {}
                override fun onFabClicked() {}
            }
        }
        val fileManager = ApplicationGraph.getFileManager()
        val fileOpenManager = ApplicationGraph.getFileOpenManager()
        val fileCopyCutManager = ApplicationGraph.getFileCopyCutManager()
        return FileColumnHorizontalListsPresenter(
                this,
                fileManager,
                fileOpenManager,
                fileCopyCutManager,
                Environment.getExternalStorageDirectory().absolutePath
        )
    }

    private fun createList(index: Int): FileColumnListView {
        val fileListView = FileColumnListView(context)
        fileListView.layoutParams = FrameLayout.LayoutParams(
                context.resources.getDimensionPixelSize(R.dimen.file_horizontal_lists_list_width),
                FrameLayout.LayoutParams.MATCH_PARENT)
        fileListView.setFileClickListener(object : FileColumnRow.FileClickListener {
            override fun onFileClicked(file: File) {
                userAction.onFileClicked(index, file)
            }
        })
        fileListView.setFileLongClickListener(object : FileColumnRow.FileLongClickListener {
            override fun onFileLongClicked(file: File) {
                userAction.onFileLongClicked(index, file)
            }
        })
        return fileListView
    }

    interface FileHorizontalListsSelectedFileListener {
        fun onSelectedFilePathChanged(path: String?)
    }
}