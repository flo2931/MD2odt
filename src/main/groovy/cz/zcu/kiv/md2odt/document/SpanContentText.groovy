package cz.zcu.kiv.md2odt.document

/**
 *
 * @version 2017-04-11
 * @author Patrik Harag
 */
class SpanContentText implements SpanContent {

    /** SpanContentText constructor.
     *
     * @param text text content
     * @param styles styles to set to a text
     * */
    SpanContentText(String text, Collection<TextStyle> styles) {
        this.text = text
        this.styles = styles
    }

    final String text
    final Set<TextStyle> styles
    final SpanType type = SpanType.TEXT

}
