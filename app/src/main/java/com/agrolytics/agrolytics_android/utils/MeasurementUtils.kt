package com.agrolytics.agrolytics_android.utils

import kotlin.math.pow

/**
 * Mérés folyamata során elvégzendő műveletek segédfüggvényeinek helper osztálya.
 *
 * @constructor Create empty Measurement utils
 */
class MeasurementUtils {
    companion object {
        /**
         * Egyedi sztring azonosítót generál egy mérési session-höz.
         * TODO: Implementálni a metódust és kitalálni, hogy mik legyenek a bemenő paraméterei
         * @return A session azonosítója.
         */
        fun generateSessionId()
        : String {

            return ""
        }

        /**
         * Kiszámolja egy sarang térfogatát a bemeneti paraméterek alapján.
         * TODO: Implementálni a metódust
         * @param numOfWoodPixels A képen szereplő sarang azon pixeleinek darabszáma, ahol fa van jelen.
         * @param rodLength A képen szereplő rúd hossza méterben mérve.
         * @param rodLengthPixel A képen szereplő rúd hossza pixelekben mérve.
         * @param woodLength A képen szereplő farakás mélysége
         * @return A kiszámolt térfogat köbméterben mérve.
         */
        fun calculateWoodVolume(
            numOfWoodPixels: Int,
            rodLength: Double,
            rodLengthPixel: Double,
            woodLength : Double
        ) : Double {
             return rodLength.pow(2) / rodLengthPixel.pow(2) * numOfWoodPixels * woodLength
        }

    }
}
