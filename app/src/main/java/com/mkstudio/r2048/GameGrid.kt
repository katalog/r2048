package com.mkstudio.r2048

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import com.mkstudio.r2048.util.myLogD

class GameGrid(val main: MainActivity, Size:Int) {
    val binding = main.binding
    val size = Size
    var gameGrid = Array(size) { Array<GameTile?>(size) { null }}
    val margin = 11

    val movedTiles = mutableListOf<GameTile>()
    val removedTiles = mutableListOf<GameTile>()
    val newTiles = mutableListOf<Triple<Int,Int,Int>>()

    val NOMOVE = 0
    val MOVE = 1
    val MERGE = 2
    val NOTHING_HAPPENED = -1

    fun initGrid() {
        for(gy in 0 until size) {
            for(gx in 0 until size) {
                val tile = gameGrid[gy][gx]
                if ( tile != null ) {
                    tile.visibility = View.GONE
                    gameGrid[gy][gx] = null
                }
            }
        }
        movedTiles.clear()
        removedTiles.clear()
        newTiles.clear()
    }

    fun getTile(ry:Int, rx:Int): GameTile? {
        return gameGrid[ry][rx]
    }

    fun setTile(ry:Int, rx:Int, tile:GameTile) {
        gameGrid[ry][rx] = tile
    }

    fun makeNewGameTile(ry: Int, rx: Int, v:Int) {
        val transition = AutoTransition()
        transition.duration = 100
        val constraintSet = ConstraintSet()

        val newTile = GameTile(main)
        newTile.id = View.generateViewId()
        newTile.setValue(v)
        newTile.setCurtPos(ry, rx)
        gameGrid[ry][rx] = newTile

        myLogD("anim NEW tile $ry, $rx")

        binding.gameContainer.addView(newTile)

        setDefaultConstraint(constraintSet, newTile.id)

        setPositionConstraint(constraintSet, newTile.id, ry, rx)

        loadAnimationPop(newTile, transition)

        constraintSet.applyTo(binding.gameContainer)
    }

    fun setDefaultConstraint(constraintSet: ConstraintSet, id :Int) {
        constraintSet.constrainHeight(id, 0)
        constraintSet.constrainWidth(id, 0)
        constraintSet.setDimensionRatio(id, "1:1")
    }

    fun setPositionConstraint(constraintSet: ConstraintSet, id :Int, py:Int, px:Int) {
        val gridLocIdName = "tile_${py}_${px}"
        val conId = main.resources.getIdentifier(gridLocIdName, "id", main.packageName)
        constraintSet.connect(id, ConstraintSet.LEFT, conId, ConstraintSet.LEFT, margin)
        constraintSet.connect(id, ConstraintSet.RIGHT, conId, ConstraintSet.RIGHT, margin)
        constraintSet.connect(id, ConstraintSet.TOP, conId, ConstraintSet.TOP, margin)
        constraintSet.connect(id, ConstraintSet.BOTTOM, conId, ConstraintSet.BOTTOM, margin)
    }

    fun loadAnimationPop(newTile: GameTile, transition: AutoTransition) {
        val pop = AnimationUtils.loadAnimation(main, R.anim.pop)
        newTile.startAnimation(pop)
        TransitionManager.beginDelayedTransition(binding.gameContainer, transition)
    }

    fun move(direction:Int) : Int {
        val vector = getVector(direction)
        val traversal = getTraversals(vector)

        // init move,merge,pos values
        for (y in traversal.first) {
            for (x in traversal.second) {
                val tile = gameGrid[y][x]
                if (tile != null) {
                    tile.curPos = Pair(y,x)
                    tile.isMerged = false
                    tile.isMoved = false
                    tile.nextPos = tile.curPos
                }
            }
        }

        for(i in 0 until size) {
            for (j in 0 until size) {
                if ( gameGrid[i][j] != null) {
                    myLogD("bf grid $i,$j -> //${gameGrid[i][j]?.getValue()}//, " +
                            "${gameGrid[i][j]?.nextPos?.first}, ${gameGrid[i][j]?.nextPos?.second}")
                }
            }
        }

        for (y in traversal.first) {
            for (x in traversal.second) {
                if ( gameGrid[y][x] == null ) continue

                val tile = gameGrid[y][x]
                if (tile != null) {
                    val ret = shiftPosition(tile, Pair(y,x), vector)
                    when (ret) {
                        MOVE -> {
                            val op = tile.curPos
                            val np = tile.nextPos
                            gameGrid[np.first][np.second] = tile
                            gameGrid[op.first][op.second] = null
                            myLogD("MOVE ${op.first},${op.second} -> ${np.first},${np.second}")
                            myLogD("move null ${op.first},${op.second}")
                        }
                        MERGE -> {
                            val op = tile.curPos
                            val np = tile.nextPos
                            gameGrid[op.first][op.second] = null
                            myLogD("merge dst ${np.first},${np.second}, <- ${op.first},${op.second}")
                            myLogD("merge null ${op.first},${op.second}")
                        }
                    }
                }
            }
        }

        if ( true == isNothingMoveMerge() ) {
            return NOTHING_HAPPENED
        }

        animMoveTiles()
        animRemoveTiles()
        val score :Int = animNewTiles()

        for(i in 0 until size) {
            for (j in 0 until size) {
                if ( gameGrid[i][j] != null) {
                    myLogD("af grid $i,$j -> ${gameGrid[i][j]?.getValue()}")
                }
            }
        }

        return score
    }

    fun shiftMove(tile:GameTile) {
        movedTiles.add(tile)
        tile.isMoved = true
    }

    fun shiftMerge(tile:GameTile, dsttile:GameTile) {
        movedTiles.add(tile)
        removedTiles.add(tile)
        removedTiles.add(dsttile)
        dsttile.isMerged = true
        newTiles.add(Triple(dsttile.nextPos.first, dsttile.nextPos.second, tile.getValue() * 2))
        tile.nextPos = dsttile.nextPos
    }

    fun shiftPosition(tile:GameTile, position:Pair<Int,Int>, vector: Pair<Int, Int>): Int{
        var prev : Pair<Int, Int>
        var pos : Pair<Int, Int>
        pos = position
        do {
            prev = pos
            val ny = prev.first + vector.first
            val nx = prev.second + vector.second
            pos = Pair(ny,nx)
            tile.setNextPos(prev.first, prev.second)
        } while(isInBoundary(pos) && isAvailable(pos))


        if ( false == isInBoundary(pos) ) {
            if ( tile.curPos != prev ) {
                // nextpos == out of boundary : move
                shiftMove(tile)
                return MOVE
            }
            // no move. skip process
            return NOMOVE
        }

        // nextpos = in boundary : move or merge
        var dsttile = gameGrid[pos.first][pos.second]

        if ( tile.curPos == prev) {
            if (dsttile?.getValue() != tile.getValue()) {
                // no move. skip process
                return NOMOVE
            } else {
                // merge
                shiftMerge(tile, dsttile)
                return MERGE
            }
        }

        if (dsttile?.getValue() != tile.getValue()) {
            // move
            shiftMove(tile)
            return MOVE
        } else {
            if (dsttile.isMerged == true) {
                shiftMove(tile)
                return MOVE
            } else {
                // merge
                shiftMerge(tile, dsttile)
                return MERGE
            }
        }

        throw UnknownError("can't reach here!")
        return NOMOVE
    }

    fun animMoveTiles() {
        val transition = AutoTransition()
        transition.duration = 100
        val constraintSet = ConstraintSet()

        for (tile in movedTiles) {
            val op = tile.curPos
            val np = tile.nextPos
            myLogD("anim MOVE ${op.first}, ${op.second} -> ${np.first},${np.second}")

            val ry = tile.nextPos.first
            val rx = tile.nextPos.second
            setDefaultConstraint(constraintSet, tile.id)
            setPositionConstraint(constraintSet, tile.id, ry, rx)
            loadAnimationPop(tile, transition)

            tile.curPos = Pair(ry,rx)
        }

        constraintSet.applyTo(binding.gameContainer)

        movedTiles.clear()
    }

    fun animRemoveTiles() {
        val transition = AutoTransition()
        transition.duration = 100
        val constraintSet = ConstraintSet()

        for (tile in removedTiles) {
            val op = tile.curPos
            val np = tile.nextPos

            val ry = tile.nextPos.first
            val rx = tile.nextPos.second
            gameGrid[ry][rx] = null
            myLogD("anim REMOVE ${np.first},${np.second}")
            myLogD("anim remove null ${np.first},${np.second}")
            tile.animate().alpha(0.0f).setDuration(100)
        }

        constraintSet.applyTo(binding.gameContainer)

        for (tile in removedTiles) {
            tile.visibility = View.GONE
        }

        removedTiles.clear()
    }

    fun animNewTiles():Int {
        var score:Int = 0
        for (p in newTiles) {
            makeNewGameTile(p.first, p.second, p.third)
            score += p.third
        }
        newTiles.clear()

        return score
    }

    fun isInBoundary(pos : Pair<Int,Int>) : Boolean {
        if (pos.first in 0 until size) {
            if ( pos.second in 0 until size) {
                return true
            }
        }
        return false
    }

    fun isAvailable(pos:Pair<Int,Int>) : Boolean {
        if ( gameGrid[pos.first][pos.second] == null)
            return true
        return false
    }

    fun getVector(direction: Int) : Pair<Int,Int> {
        val UP = 0
        val RIGHT = 1
        val DOWN = 2
        val LEFT = 3

        var vector : Pair<Int,Int>

        vector = when(direction) {
            UP -> Pair(-1,0)
            DOWN -> Pair(1, 0)
            LEFT -> Pair(0, -1)
            RIGHT -> Pair(0, 1)
            else -> throw IllegalArgumentException("getVector direction wrong")
        }

        return vector
    }

    fun getTraversals(vector:Pair<Int,Int>) : Pair<IntArray, IntArray> {
        val ya = IntArray(size, {i->i})
        val xa = IntArray(size, {i->i})
        if (vector.first == 1) {
            ya.reverse()
        } else if ( vector.second == 1) {
            xa.reverse()
        }

        return Pair(ya,xa)
    }

    fun getTilesNumber():Int {
        var count = 0
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (gameGrid[y][x] != null) {
                    count++
                }
            }
        }
        return count
    }

    fun isNothingMoveMerge(): Boolean {
        if ( 0 == movedTiles.count() + removedTiles.count() + newTiles.count() ) {
            return true
        }

        return false
    }
}