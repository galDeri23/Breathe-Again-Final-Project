package com.example.finalproject_breathe_again.ui.craving.game

class GameManager(
    private val lifeCount: Int = 3,
    private val numLanes: Int = 3,
    private val numRows: Int = 5
) {
    var score: Int = 0
        private set

    var boyIndex: Int = 1
        private set

    var timesHits: Int = 0
        private set

    val cigarettesMatrix: Array<Array<Boolean>> = Array(numRows) { Array(numLanes) { false } }
    val currencyMatrix: Array<Array<Boolean>> = Array(numRows) { Array(numLanes) { false } }

    val isGameOver: Boolean
        get() = timesHits == lifeCount

    fun moveLeft() {
        if (boyIndex > 0) {
            boyIndex--
        }
    }

    fun moveRight() {
        if (boyIndex < numLanes - 1) {
            boyIndex++
        }
    }

    fun hitsCigarettes() {
        timesHits++
    }
    fun hitsCurrency() {
        score += 10
    }

    fun resetGame() {
        boyIndex = 1
        timesHits = 0
        cigarettesMatrix.forEach { it.fill(false) }
        randomizeCigarettesAndCurrency()
    }

    fun checkCollision(): Boolean {
        return cigarettesMatrix[numRows - 1][boyIndex]
    }
    fun checkCollisionCurrency(): Boolean {
        return currencyMatrix[numRows - 1][boyIndex]
    }

    fun moveCigarettes() {
        for (i in numRows - 1 downTo 1) {
            for (j in 0 until numLanes) {
                cigarettesMatrix[i][j] = cigarettesMatrix[i - 1][j]
            }
        }

        for (j in 0 until numLanes) {
            cigarettesMatrix[0][j] = false
        }
    }
    fun moveCurrency() {
        for (i in numRows - 1 downTo 1) {
            for (j in 0 until numLanes) {
                currencyMatrix[i][j] = currencyMatrix[i - 1][j]
            }
        }

        for (j in 0 until numLanes) {
            currencyMatrix[0][j] = false
        }
    }

    fun randomizeCigarettesAndCurrency() {
        val firstRow = 0
        val randNum = (0..<numLanes).random()
        var randNum1 = (0..<numLanes).random()
        while (randNum == randNum1){
            randNum1 = (0..<numLanes).random()
        }
        for (i in 0 until numLanes) {
            cigarettesMatrix[firstRow][randNum] = true
            currencyMatrix[firstRow][randNum1] = true
        }
    }

}