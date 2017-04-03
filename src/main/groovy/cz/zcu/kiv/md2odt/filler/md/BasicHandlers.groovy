package cz.zcu.kiv.md2odt.filler.md

import com.vladsch.flexmark.ast.Node as AstNode
import cz.zcu.kiv.md2odt.document.Document
import groovy.transform.Immutable

/**
 *
 * @version 2017-04-02
 * @author Patrik Harag
 */
class BasicHandlers {

    private BasicHandlers() {}


    static AstNodeHandler ignore(Class<?> target) {
        new IgnoreHandler(target: target)
    }

    @Immutable
    private static class IgnoreHandler implements AstNodeHandler {

        Class<?> target

        @Override
        void handle(AstNode node, Context context, Document document) {
            // nothing
        }
    }

}