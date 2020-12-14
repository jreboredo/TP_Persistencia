package ar.edu.unq.eperdemic.utils

import java.lang.Integer.*

val addWithMax = { firstValue : Int, secondValue : Int, maxValue : Int -> min(maxValue, firstValue.plus(secondValue)) }

val subWithMin = { firstValue : Int, secondValue : Int, minValue : Int -> max(minValue, firstValue.minus(secondValue)) }
