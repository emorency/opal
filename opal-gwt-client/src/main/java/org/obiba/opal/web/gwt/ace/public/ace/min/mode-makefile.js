define("ace/mode/makefile", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/tokenizer", "ace/mode/makefile_highlight_rules", "ace/mode/folding/coffee"], function (e, t, n) {
    var r = e("../lib/oop"), i = e("./text").Mode, s = e("../tokenizer").Tokenizer, o = e("./makefile_highlight_rules").MakefileHighlightRules, u = e("./folding/coffee").FoldMode, a = function () {
        var e = new o;
        this.foldingRules = new u, this.$tokenizer = new s(e.getRules())
    };
    r.inherits(a, i), function () {
    }.call(a.prototype), t.Mode = a
}), define("ace/mode/makefile_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules", "ace/mode/sh_highlight_rules"], function (e, t, n) {
    var r = e("../lib/oop"), i = e("./text_highlight_rules").TextHighlightRules, s = e("./sh_highlight_rules"), o = function () {
        var e = this.createKeywordMapper({keyword: s.reservedKeywords, "support.function.builtin": s.languageConstructs, "invalid.deprecated": "debugger"}, "string");
        this.$rules = {start: [
            {token: "string.interpolated.backtick.makefile", regex: "`", next: "shell-start"},
            {token: "punctuation.definition.comment.makefile", regex: /#(?=.)/, next: "comment"},
            {token: ["keyword.control.makefile"], regex: "^(?:\\s*\\b)(\\-??include|ifeq|ifneq|ifdef|ifndef|else|endif|vpath|export|unexport|define|endef|override)(?:\\b)"},
            {token: ["entity.name.function.makefile", "text"], regex: "^([^\\t ]+(?:\\s[^\\t ]+)*:)(\\s*.*)"}
        ], comment: [
            {token: "punctuation.definition.comment.makefile", regex: /.+\\/},
            {token: "punctuation.definition.comment.makefile", regex: ".+", next: "start"}
        ], "shell-start": [
            {token: e, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"},
            {token: "string", regex: "\\w+"},
            {token: "string.interpolated.backtick.makefile", regex: "`", next: "start"}
        ]}
    };
    r.inherits(o, i), t.MakefileHighlightRules = o
}), define("ace/mode/sh_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (e, t, n) {
    var r = e("../lib/oop"), i = e("./text_highlight_rules").TextHighlightRules, s = t.reservedKeywords = "!|{|}|case|do|done|elif|else|esac|fi|for|if|in|then|until|while|&|;|export|local|read|typeset|unset|elif|select|set", o = t.languageConstructs = "[|]|alias|bg|bind|break|builtin|cd|command|compgen|complete|continue|dirs|disown|echo|enable|eval|exec|exit|fc|fg|getopts|hash|help|history|jobs|kill|let|logout|popd|printf|pushd|pwd|return|set|shift|shopt|source|suspend|test|times|trap|type|ulimit|umask|unalias|wait", u = function () {
        var e = this.createKeywordMapper({keyword: s, "support.function.builtin": o, "invalid.deprecated": "debugger"}, "identifier"), t = "(?:(?:[1-9]\\d*)|(?:0))", n = "(?:\\.\\d+)", r = "(?:\\d+)", i = "(?:(?:" + r + "?" + n + ")|(?:" + r + "\\.))", u = "(?:(?:" + i + "|" + r + ")" + ")", a = "(?:" + u + "|" + i + ")", f = "(?:&" + r + ")", l = "[a-zA-Z][a-zA-Z0-9_]*", c = "(?:(?:\\$" + l + ")|(?:" + l + "=))", h = "(?:\\$(?:SHLVL|\\$|\\!|\\?))", p = "(?:" + l + "\\s*\\(\\))";
        this.$rules = {start: [
            {token: ["text", "comment"], regex: /(^|\s)(#.*)$/},
            {token: "string", regex: '"(?:[^\\\\]|\\\\.)*?"'},
            {token: "variable.language", regex: h},
            {token: "variable", regex: c},
            {token: "support.function", regex: p},
            {token: "support.function", regex: f},
            {token: "string", regex: "'(?:[^\\\\]|\\\\.)*?'"},
            {token: "constant.numeric", regex: a},
            {token: "constant.numeric", regex: t + "\\b"},
            {token: e, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"},
            {token: "keyword.operator", regex: "\\+|\\-|\\*|\\*\\*|\\/|\\/\\/|~|<|>|<=|=>|=|!="},
            {token: "paren.lparen", regex: "[\\[\\(\\{]"},
            {token: "paren.rparen", regex: "[\\]\\)\\}]"}
        ]}
    };
    r.inherits(u, i), t.ShHighlightRules = u
}), define("ace/mode/folding/coffee", ["require", "exports", "module", "ace/lib/oop", "ace/mode/folding/fold_mode", "ace/range"], function (e, t, n) {
    var r = e("../../lib/oop"), i = e("./fold_mode").FoldMode, s = e("../../range").Range, o = t.FoldMode = function () {
    };
    r.inherits(o, i), function () {
        this.getFoldWidgetRange = function (e, t, n) {
            var r = this.indentationBlock(e, n);
            if (r)return r;
            var i = /\S/, o = e.getLine(n), u = o.search(i);
            if (u == -1 || o[u] != "#")return;
            var a = o.length, f = e.getLength(), l = n, c = n;
            while (++n < f) {
                o = e.getLine(n);
                var h = o.search(i);
                if (h == -1)continue;
                if (o[h] != "#")break;
                c = n
            }
            if (c > l) {
                var p = e.getLine(c).length;
                return new s(l, a, c, p)
            }
        }, this.getFoldWidget = function (e, t, n) {
            var r = e.getLine(n), i = r.search(/\S/), s = e.getLine(n + 1), o = e.getLine(n - 1), u = o.search(/\S/), a = s.search(/\S/);
            if (i == -1)return e.foldWidgets[n - 1] = u != -1 && u < a ? "start" : "", "";
            if (u == -1) {
                if (i == a && r[i] == "#" && s[i] == "#")return e.foldWidgets[n - 1] = "", e.foldWidgets[n + 1] = "", "start"
            } else if (u == i && r[i] == "#" && o[i] == "#" && e.getLine(n - 2).search(/\S/) == -1)return e.foldWidgets[n - 1] = "start", e.foldWidgets[n + 1] = "", "";
            return u != -1 && u < i ? e.foldWidgets[n - 1] = "start" : e.foldWidgets[n - 1] = "", i < a ? "start" : ""
        }
    }.call(o.prototype)
})