package com.anwesh.uiprojects.linkedsweepview

/**
 * Created by anweshmishra on 01/07/18.
 */

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

val SWEEP_NODES : Int = 5

class LinkedSweepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb  : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SweepNode(var i : Int, val state : State = State()) {

        private var next : SweepNode? = null

        private var prev : SweepNode? = null

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val getDeg : (Int) -> Float = {i -> 180f + 180f * state.scales[i]}
            canvas.drawWithGap(i) {canvas, gap ->
                canvas.drawSemiPie(0f, 0f, gap/2, getDeg(1), getDeg(0), paint)
            }
        }

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < SWEEP_NODES) {
                next = SweepNode(i + 1)
                next?.prev = this
            }
        }

        fun getNext(dir : Int, cb : () -> Unit)  : SweepNode {
            var curr : SweepNode? = prev
            if (dir == 1) {
                curr = prev
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

    }

    data class LinkedSweep(var i : Int) {

        private var curr : SweepNode = SweepNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedSweepView) {

        private val animator : Animator = Animator(view)

        private val linkedSweep : LinkedSweep = LinkedSweep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            paint.color = Color.parseColor("#F57F17")
            linkedSweep.draw(canvas, paint)
            animator.animate {
                linkedSweep.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedSweep.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LinkedSweepView {
            val view : LinkedSweepView = LinkedSweepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}

fun Canvas.getSize() : PointF {
    return PointF(width.toFloat(), height.toFloat())
}

fun Canvas.drawSemiPie(x : Float, y : Float, r : Float, start : Float, end : Float, paint : Paint) {
    save()
    translate(x, y)
    drawArc(RectF(-r, -r, r, r), start, (end - start), true, paint)
    restore()
}

fun Canvas.drawAtPoint(x : Float, y : Float, cb : (Canvas) -> Unit) {
    save()
    translate(x, y)
    cb(this)
    restore()
}

fun Canvas.drawWithGap(i : Int, cb : (Canvas, Float) -> Unit) {
    val gap : Float = getSize().x / SWEEP_NODES
    drawAtPoint(i * gap + gap / 2, getSize().y / 2) {
        cb(it, gap)
    }
}