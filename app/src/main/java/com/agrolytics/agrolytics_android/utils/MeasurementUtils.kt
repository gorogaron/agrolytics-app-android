package com.agrolytics.agrolytics_android.utils

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
         * @return A kiszámolt térfogat köbméterben mérve.
         */
        fun calculateWoodVolume(
            numOfWoodPixels: Int,
            rodLength: Float,
            rodLengthPixel: Float
        ) : Float {

            return 1f
        }
    }
}
