package com.mkstudio.r2048

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.mkstudio.r2048.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val grid by lazy { GameGrid(this, size) }
    val margin = 11
    val size = 4
    var score = 0
    var bestScore = 0
    val NOTHING_HAPPENED = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnNewGame.setOnClickListener {
            initGame()
        }

        bestScore = loadBestScore()
        binding.tvBestScore.text = "$bestScore"
    }

    fun initGame() {
        score = 0
        grid.initGrid()
        binding.tvScore.text = score.toString()

        makeRandomTile(2)
        binding.btnNewGame.isEnabled = false

        binding.ctTouchLayout.setOnTouchListener(
            object: OnSwipeTouchListener(this@MainActivity){})
    }

    fun move(direction: Int) {
        val movescore = grid.move(direction)

        if ( movescore == NOTHING_HAPPENED ) {
            // end game check
            if ( true == isGameOVer() ) {
                processGameOver()
            }

            return
        }

        updateScore(movescore)

        // make a tile in random position
        makeRandomTile(1)
    }

    fun isGameOVer() : Boolean {
        for (x in 0 until size) {
            for (y in 0 until size-1) {
                if ( y+1 < size) {
                    if (grid.gameGrid[y][x]?.getValue() == grid.gameGrid[y + 1][x]?.getValue())
                        return false
                }
                if ( x+1 < size) {
                    if (grid.gameGrid[y][x]?.getValue() == grid.gameGrid[y][x + 1]?.getValue())
                        return false
                }
            }
        }
        return true
    }

    fun makeRandomTile(count:Int) {
        var newTileCount = 0
        val maxNewTileCount = count

        do{
            var ry = Random.nextInt(0, 4)
            var rx = Random.nextInt(0, 4)
            if ( grid.getTile(ry, rx) == null ) {
                grid.makeNewGameTile(ry, rx, 2)
                newTileCount++
            }
        } while(newTileCount < maxNewTileCount)
    }

    fun updateScore(movescore:Int) {
        score += movescore
        binding.tvScore.text = "$score"
        if (score > bestScore) {
            bestScore = score
            binding.tvBestScore.text = "$bestScore"
        }
        saveBestScore()
    }

    fun loadBestScore():Int {
        // read/write shared preferences
        // https://developer.android.com/training/data-storage/shared-preferences?hl=ko
        var nScore = 0
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return 0
        nScore = sharedPref.getInt(getString(R.string.key_best_score), 0)
        return nScore
    }

    fun saveBestScore() {
        // read/write shared preferences
        // https://developer.android.com/training/data-storage/shared-preferences?hl=ko
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt(getString(R.string.key_best_score), bestScore)
            commit()
        }
    }

    fun processGameOver() {
        saveBestScore()
        binding.btnNewGame.isEnabled = true
        binding.ctTouchLayout.setOnTouchListener { _, _ -> true }

        var dialog = GameOverFragmentDialog(score)
        dialog.show(supportFragmentManager, "Game Over Dialog")
    }

    /// for debug
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val UP = 0
        val RIGHT = 1
        val DOWN = 2
        val LEFT = 3

        when(keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> move(UP)
            KeyEvent.KEYCODE_DPAD_DOWN -> move(DOWN)
            KeyEvent.KEYCODE_DPAD_RIGHT -> move(RIGHT)
            KeyEvent.KEYCODE_DPAD_LEFT -> move(LEFT)
        }
        return super.onKeyUp(keyCode, event)
    }
}