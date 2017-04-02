package helpers

/**
 * Created by hozer on 2.4.2017.
 */
class TextCaseHelper {

    public static String camelCaseToUnderscore(String cameCaseText) {
        return cameCaseText.replaceAll(/\B[A-Z]/) { '_' + it };
    }

    public static String camelCaseToUpperUnderscore(String cameCaseText, Locale locale) {
        return camelCaseToUnderscore(cameCaseText).toUpperCase(locale)
    }

    public static String camelCaseToLowerUnderscore(String cameCaseText, Locale locale) {
        return camelCaseToUnderscore(cameCaseText).toLowerCase(locale)
    }

}
