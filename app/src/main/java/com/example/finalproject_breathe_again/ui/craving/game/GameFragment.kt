package com.example.finalproject_breathe_again.ui.craving.game


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finalproject_breathe_again.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameFragment : Fragment() {

    private lateinit var right_arrow: FloatingActionButton
    private lateinit var left_arrow: FloatingActionButton
    private lateinit var cigarettes: Array<Array<AppCompatImageView>>
    private lateinit var currency00 : Array<Array<AppCompatImageView>>
    private lateinit var guy: Array<AppCompatImageView>
    private lateinit var hearts: Array<AppCompatImageView>
    private lateinit var score: MaterialTextView
    private lateinit var gameManager: GameManager
    private var startTime: Long = 0
    private var timerOn: Boolean = false
    private lateinit var timerJob: Job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViews(view)
        gameManager = GameManager(hearts.size)
        initViews()
        startTimer()
    }


    private fun restartGame() {
        gameManager.resetGame()
        updateHearts()
        updateUI()
        startTimer()
    }

    private fun startTimer() {
        if (!timerOn) {
            startTime = System.currentTimeMillis()
            timerOn = true
            timerJob = lifecycleScope.launch {
                while (timerOn) {
                    delay(700L)
                    moveDownCigarettesAndCurrency()
                    updateCigarettes()
                    updateCurrency()
                    if (gameManager.isGameOver) {
                        stopTimer()
                        restartGame()
                    }
                }
            }
        }
    }
    private fun stopTimer() {
        timerOn = false
        if (::timerJob.isInitialized) {
            timerJob.cancel()
        }
    }

    private fun initViews() {
        right_arrow.setOnClickListener({ v -> moveRight() })
        left_arrow.setOnClickListener({ v -> moveLeft() })
        gameManager.randomizeCigarettesAndCurrency()
        updateUI()
    }

    private fun moveLeft() {
        gameManager.moveLeft()
        updateUI()
    }
    private fun moveRight() {
        gameManager.moveRight()
        updateUI()
    }

    private fun updateUI() {
        updateBoys()
        updateCigarettes()
        updateCurrency()

    }
    private fun checkCollisionCigarettes() {
        if (gameManager.checkCollision()) {
            gameManager.hitsCigarettes()
            hearts[hearts.size - gameManager.timesHits].visibility =
                View.INVISIBLE
            toast("You hit an asteroid!")
            if (gameManager.isGameOver) {
                stopTimer()
                toast("Game Over! Try again.")
                restartGame()
            }
        }
    }
    private fun checkCollisionCurrency() {
        if (gameManager.checkCollisionCurrency()) {
            gameManager.hitsCurrency()
            score.text = gameManager.score.toString()
            toast("You collected currency!")
        }
    }
    private fun updateHearts() {
        for (i in hearts.indices) {
            hearts[i].visibility = View.VISIBLE
        }
    }
    private fun moveDownCigarettesAndCurrency() {
        gameManager.moveCigarettes()
        gameManager.moveCurrency()
        updateCigarettes()
        updateCurrency()
        gameManager.randomizeCigarettesAndCurrency()
        checkCollisionCigarettes()
        checkCollisionCurrency()
    }
    private fun updateCigarettes() {
        for (i in cigarettes.indices) {
            for (j in cigarettes[i].indices) {
                if (gameManager.cigarettesMatrix[i][j]) {
                    cigarettes[i][j].visibility = View.VISIBLE
                } else {
                    cigarettes[i][j].visibility = View.INVISIBLE
                }
            }
        }
    }
    private fun updateCurrency() {
        for (i in currency00.indices) {
            for (j in currency00[i].indices) {
                if (gameManager.currencyMatrix[i][j]) {
                    currency00[i][j].visibility = View.VISIBLE
                } else {
                    currency00[i][j].visibility = View.INVISIBLE
                }
            }
        }
    }
    private fun updateBoys() {
        for (i in guy.indices) {
            if (i == gameManager.boyIndex) {
                guy[i].visibility = View.VISIBLE
            } else {
                guy[i].visibility = View.INVISIBLE
            }
        }
    }

    private fun toast(text: String) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
    private fun findViews(view: View) {
        right_arrow = view.findViewById(R.id.right_arrow)
        left_arrow = view.findViewById(R.id.left_arrow)
        hearts = arrayOf(
            view.findViewById(R.id.hart1),
            view.findViewById(R.id.hart2),
            view.findViewById(R.id.hart3)
        )
        cigarettes = arrayOf(
            arrayOf(
                view.findViewById(R.id.cigarette00),
                view.findViewById(R.id.cigarette01),
                view.findViewById(R.id.cigarette02)
            ),
            arrayOf(
                view.findViewById(R.id.cigarette10),
                view.findViewById(R.id.cigarette11),
                view.findViewById(R.id.cigarette12)
            ),
            arrayOf(
                view.findViewById(R.id.cigarette20),
                view.findViewById(R.id.cigarette21),
                view.findViewById(R.id.cigarette22)
            ),
            arrayOf(
                view.findViewById(R.id.cigarette30),
                view.findViewById(R.id.cigarette31),
                view.findViewById(R.id.cigarette32)
            ),
            arrayOf(
                view.findViewById(R.id.cigarette40),
                view.findViewById(R.id.cigarette41),
                view.findViewById(R.id.cigarette42)
            )
        )
        guy = arrayOf(
            view.findViewById(R.id.guy1),
            view.findViewById(R.id.guy2),
            view.findViewById(R.id.guy3)
        )
        score = view.findViewById(R.id.score)

        currency00 = arrayOf(
            arrayOf(
                view.findViewById(R.id.currency00),
                view.findViewById(R.id.currency01),
                view.findViewById(R.id.currency02)
            ),
            arrayOf(
                view.findViewById(R.id.currency10),
                view.findViewById(R.id.currency11),
                view.findViewById(R.id.currency12)
            ),
            arrayOf(
                view.findViewById(R.id.currency20),
                view.findViewById(R.id.currency21),
                view.findViewById(R.id.currency22)
            ),
            arrayOf(
                view.findViewById(R.id.currency30),
                view.findViewById(R.id.currency31),
                view.findViewById(R.id.currency32)
            ),
            arrayOf(
                view.findViewById(R.id.currency40),
                view.findViewById(R.id.currency41),
                view.findViewById(R.id.currency42)
            )
        )
    }
}



