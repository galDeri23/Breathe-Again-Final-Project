package com.example.finalproject_breathe_again.ui.craving.story

data class Story(
    val id: String,
    val title: String,
    val overview: String,
    val date: String,
    val imageUrl: String,
    val author: String,
    var isExpanded: Boolean = false
) {
    constructor() : this("", "", "", "", "", "")
    class Builder {
        private var id: String = ""
        private var title: String = ""
        private var overview: String = ""
        private var date: String = ""
        private var imageUrl: String = ""
        private var author: String = ""

        fun id(id: String) = apply { this.id = id }
        fun title(title: String) = apply { this.title = title }
        fun overview(overview: String) = apply { this.overview = overview }
        fun date(date: String) = apply { this.date = date }
        fun imageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }
        fun author(author: String) = apply { this.author = author }

        fun build() = Story(id, title, overview, date, imageUrl, author)
    }
}