package person

import Gender

class Manager(name: String,
              age: Int,
              gender: Gender) : Person(name, age, gender) {
}