package cz.zcu.kiv.md2odt.highlight

import cz.zcu.kiv.md2odt.highlight.content.CodeLang
import cz.zcu.kiv.md2odt.highlight.content.CodeSection
import cz.zcu.kiv.md2odt.highlight.content.CodeSectionImpl
import cz.zcu.kiv.md2odt.highlight.content.CodeSectionType
import org.apache.log4j.Logger
import org.python.core.PyList
import org.python.core.PyTuple
import org.python.core.PyTupleDerived
import org.python.util.PythonInterpreter

/**
 * Highlighter implementation
 *
 * @version 2017-04-06
 * @author Vít Mazín
 */
class CodeParser implements Parser {
    private static final Logger LOGGER = Logger.getLogger(CodeParser)
    private CodeLang langForHighlight = null
    private String lastLang = null

    /** Returns a list of CodeSection, where is specified how to style a text segment.
     *
     * @param code  Code to parse.
     * @param lang  Language used.
     * @return list of CodeSection
     */
    @Override
    List<CodeSection> parse(String code, String lang) {
        List<CodeSection> list = new ArrayList<>()

        if(lang != lastLang) {
            langForHighlight = switchLang(lang)
        }

        String lexer = langForHighlight.getLexer()

        PythonInterpreter interpreter = new PythonInterpreter()

        // Set a variable with the content you want to work with
        interpreter.set("code", code)

        interpreter.exec("""
        from pygments import highlight
        from pygments.lexers import ${lexer}
        from pygments.formatter import Formatter
        ret = None
        class OwnFormatter(Formatter):
                
            def format(self, tokensource, outfile):
                enc = self.encoding
                global ret
                ret = list(tokensource)
        
        highlight(code, ${lexer}(), OwnFormatter())""".stripIndent())

        PyList result = interpreter.get("ret", PyList.class)
        for(int i = 0; i < result.size(); i++) {
            PyTuple tuple = (PyTuple)result.get(i)
            PyTupleDerived nestedTuple = (PyTupleDerived)tuple.get(0)

            String value = tuple.get(1)
            String token = nestedTuple.get(0)
            CodeSectionType type = switchType(token)

            list.add(new CodeSectionImpl(value, type))
        }

        return list
    }

    /** Returns code snippet language.
     *
     * @param lang  name of lang
     * @return instance of CodeLang
     */
    private static CodeLang switchLang(String lang) {
        CodeLang codeLang = null

        if(lang == null) {
            codeLang = CodeLang.NONE
        } else {
            String lowerLang = lang.toLowerCase()

            for (int i = 0; i < CodeLang.values().size(); i++) {
                CodeLang pickedLang = CodeLang.values()[i]

                for(String name : pickedLang.getLangNames()) {
                    if(name.equals(lowerLang)) {
                        codeLang = pickedLang

                        LOGGER.info("usinge lexer for \"" + pickedLang.getLangNames()[0] + "\"")

                        return codeLang
                    }
                }
            }

            if (codeLang == null) {
                codeLang = CodeLang.NONE
                LOGGER.info("unknown code lang " + lang)
            }
        }

        return codeLang
    }

    /** Returns code section type based on token name.
     *
     * @param type  token name
     * @return CodeSectionType
     */
    static CodeSectionType switchType(String type) {
        CodeSectionType codeSectionType = null

        for(int i = 0; i < CodeSectionType.values().size(); i++) {
            CodeSectionType picked = CodeSectionType.values()[i]

            if(picked.getName() == type) {
                codeSectionType = picked
            }
        }

        if(codeSectionType == null) {
            codeSectionType = CodeSectionType.NONE
            LOGGER.info("unknown CodeSectionType " + type)
        }

        return codeSectionType
    }

    /** Determines if given lang is supported.
     *
     * @param lang  code snippet lang
     * @return true if lang is known or false
     */
    boolean isKnownLanguage(String lang) {
        langForHighlight = switchLang(lang)

        if (langForHighlight != null && langForHighlight != CodeLang.NONE) {
            lastLang = lang
            return true
        }

        else {
            return false
        }
    }
}
