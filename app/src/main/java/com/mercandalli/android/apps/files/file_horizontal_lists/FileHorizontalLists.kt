package com.mercandalli.android.apps.files.file_horizontal_lists

import android.content.Context
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.mercandalli.android.apps.files.R
import com.mercandalli.android.apps.files.file_list.FileListView
import com.mercandalli.android.apps.files.file_row.FileRow
import com.mercandalli.android.apps.files.main.ApplicationGraph
import com.mercandalli.sdk.files.api.File

class FileHorizontalLists @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FileHorizontalListsContract.Screen {

    private val userAction: FileHorizontalListsContract.UserAction
    private val fileListViews = ArrayList<FileListView>()
    private val fab: FloatingActionButton
    private val fileListViewContainer: LinearLayout
    private val horizontalScrollView: HorizontalScrollView

    init {
        View.inflate(context, R.layout.view_file_horizontal_lists, this)
        fab = findViewById(R.id.view_file_horizontal_lists_fab)
        fileListViewContainer = findViewById(R.id.view_file_horizontal_lists_list_view_container)
        horizontalScrollView = findViewById(R.id.view_file_horizontal_lists_horizontal_scroll_view)

        val fileOpenManager = ApplicationGraph.getFileOpenManager()
        val fileCopyCutManager = ApplicationGraph.getFileCopyCutManager()
        userAction = FileHorizontalListsPresenter(
                this,
                fileOpenManager,
                fileCopyCutManager,
                Environment.getExternalStorageDirectory().absolutePath
        )

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

        horizontalScrollView.scrollBarFadeDuration = 10000
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

    fun onResume() {
        for (fileListView in fileListViews) {
            fileListView.onResume()
        }
    }

    private fun createList(index: Int): FileListView {
        val fileListView = FileListView(context)
        fileListView.layoutParams = FrameLayout.LayoutParams(
                context.resources.getDimensionPixelSize(R.dimen.file_horizontal_lists_list_width),
                FrameLayout.LayoutParams.MATCH_PARENT)
        fileListView.setFileClickListener(object : FileRow.FileClickListener {
            override fun onFileClicked(file: File) {
                userAction.onFileClicked(index, file)
            }
        })
        fileListView.setFileLongClickListener(object : FileRow.FileLongClickListener {
            override fun onFileLongClicked(file: File) {
                userAction.onFileLongClicked(index, file)
            }
        })
        return fileListView
    }
}