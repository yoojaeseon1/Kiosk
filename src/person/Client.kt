package person

import Gender

class Client(name: String,
             age: Int,
             gender: Gender,
             var balance: Int = 0,
             var membershipPoint: Int = 0) : Person(name, age, gender) {


     fun updateBalance(totalPrice: Int) {

     }
}