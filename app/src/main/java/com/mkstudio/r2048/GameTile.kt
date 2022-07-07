package com.mkstudio.r2048

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class GameTile: AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {}

    private var innerValue = 0
    public fun getValue() : Int {
        return innerValue
    }

    var curPos = Pair(0,0)
    var nextPos = Pair(0,0)
    var isMerged = false
    var isMoved = false

    init {
        textAlignment = TEXT_ALIGNMENT_CENTER
        gravity = Gravity.CENTER
        setValue(2048)
        setTypeface(this.typeface, Typeface.BOLD)
    }

    fun setValue(v: Int) {
        innerValue = v
        text = innerValue.toString()
        var rid = getResources().getColor(R.color.tile2)

        if ( v / 1000 >= 1) {
            textSize = 30f
        } else if( v / 100 >= 1) {
            textSize = 35f
        } else {
            textSize = 40f
        }

        if (v > 8) {
            val cid = getResources().getColor(R.color.offWhiteText)
            setTextColor(cid)
        }

        when(v) {
            4 -> rid = getResources().getColor(R.color.tile4)
            8 -> rid = getResources().getColor(R.color.tile8)
            16 -> rid = getResources().getColor(R.color.tile16)
            32 -> rid = getResources().getColor(R.color.tile32)
            64 -> rid = getResources().getColor(R.color.tile64)
            128 -> rid = getResources().getColor(R.color.tile128)
            256 -> rid = getResources().getColor(R.color.tile256)
            512 -> rid = getResources().getColor(R.color.tile512)
            1024 -> rid = getResources().getColor(R.color.tile1024)
            else -> rid = getResources().getColor(R.color.tile2048)
        }
        setBackgroundColor(rid)
    }

    fun setCurtPos(ry:Int, rx:Int) {
        curPos = Pair(ry, rx)
    }
    fun setNextPos(ry:Int, rx:Int) {
        nextPos = Pair(ry, rx)
    }
}