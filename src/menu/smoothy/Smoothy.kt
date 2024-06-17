package menu.smoothy

import menu.Food

open class Smoothy(name: String,
              description: String,
              price: Int,) : Food(name, description, price) {

    override fun displyInfo() {
        super.displyInfo()
        println("시원한 스무디에요~")
    }
}