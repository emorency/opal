define("ace/ext/textarea", ["require", "exports", "module", "ace/lib/event", "ace/lib/useragent", "ace/lib/net", "ace/ace", "ace/theme/textmate", "ace/mode/text"], function (e, t, n) {
    function a(e, t) {
        for (var n in t)e.style[n] = t[n]
    }

    function f(e, t) {
        if (e.type != "textarea")throw"Textarea required!";
        var n = e.parentNode, i = document.createElement("div"), s = function () {
            var t = "position:relative;";
            ["margin-top", "margin-left", "margin-right", "margin-bottom"].forEach(function (n) {
                t += n + ":" + u(e, i, n) + ";"
            });
            var n = u(e, i, "width") || e.clientWidth + "px", r = u(e, i, "height") || e.clientHeight + "px";
            t += "height:" + r + ";width:" + n + ";", t += "display:inline-block;", i.setAttribute("style", t)
        };
        r.addListener(window, "resize", s), s(), n.insertBefore(i, e.nextSibling);
        while (n !== document) {
            if (n.tagName.toUpperCase() === "FORM") {
                var o = n.onsubmit;
                n.onsubmit = function (n) {
                    e.value = t(), o && o.call(this, n)
                };
                break
            }
            n = n.parentNode
        }
        return i
    }

    function l(t, n, r) {
        s.loadScript(t, function () {
            e([n], r)
        })
    }

    function c(n, r, i, s, o, u) {
        function c(e) {
            return e === "true" || e == 1
        }

        var a = n.getSession(), f = n.renderer;
        u = u || l, n.setDisplaySettings = function (e) {
            e == null && (e = i.style.display == "none"), e ? (i.style.display = "block", i.hideButton.focus(), n.on("focus", function t() {
                n.removeListener("focus", t), i.style.display = "none"
            })) : n.focus()
        }, n.setOption = function (t, i) {
            if (o[t] == i)return;
            switch (t) {
                case"gutter":
                    f.setShowGutter(c(i));
                    break;
                case"mode":
                    i != "text" ? u("mode-" + i + ".js", "ace/mode/" + i, function () {
                        var t = e("../mode/" + i).Mode;
                        a.setMode(new t)
                    }) : a.setMode(new (e("../mode/text").Mode));
                    break;
                case"theme":
                    i != "textmate" ? u("theme-" + i + ".js", "ace/theme/" + i, function () {
                        n.setTheme("ace/theme/" + i)
                    }) : n.setTheme("ace/theme/textmate");
                    break;
                case"fontSize":
                    r.style.fontSize = i;
                    break;
                case"keybindings":
                    switch (i) {
                        case"vim":
                            n.setKeyboardHandler("ace/keyboard/vim");
                            break;
                        case"emacs":
                            n.setKeyboardHandler("ace/keyboard/emacs");
                            break;
                        default:
                            n.setKeyboardHandler(null)
                    }
                    break;
                case"softWrap":
                    switch (i) {
                        case"off":
                            a.setUseWrapMode(!1), f.setPrintMarginColumn(80);
                            break;
                        case"40":
                            a.setUseWrapMode(!0), a.setWrapLimitRange(40, 40), f.setPrintMarginColumn(40);
                            break;
                        case"80":
                            a.setUseWrapMode(!0), a.setWrapLimitRange(80, 80), f.setPrintMarginColumn(80);
                            break;
                        case"free":
                            a.setUseWrapMode(!0), a.setWrapLimitRange(null, null), f.setPrintMarginColumn(80)
                    }
                    break;
                case"useSoftTabs":
                    a.setUseSoftTabs(c(i));
                    break;
                case"showPrintMargin":
                    f.setShowPrintMargin(c(i));
                    break;
                case"showInvisibles":
                    n.setShowInvisibles(c(i))
            }
            o[t] = i
        }, n.getOption = function (e) {
            return o[e]
        }, n.getOptions = function () {
            return o
        };
        for (var h in t.options)n.setOption(h, t.options[h]);
        return n
    }

    function h(e, t, n, i) {
        function f(e, t, n, r) {
            if (!n) {
                e.push("<input type='checkbox' title='", t, "' ", r == "true" ? "checked='true'" : "", "'></input>");
                return
            }
            e.push("<select title='" + t + "'>");
            for (var i in n)e.push("<option value='" + i + "' "), r == i && e.push(" selected "), e.push(">", n[i], "</option>");
            e.push("</select>")
        }

        var s = null, o = {mode: "Mode:", gutter: "Display Gutter:", theme: "Theme:", fontSize: "Font Size:", softWrap: "Soft Wrap:", keybindings: "Keyboard", showPrintMargin: "Show Print Margin:", useSoftTabs: "Use Soft Tabs:", showInvisibles: "Show Invisibles"}, u = {mode: {text: "Plain", javascript: "JavaScript", xml: "XML", html: "HTML", css: "CSS", scss: "SCSS", python: "Python", php: "PHP", java: "Java", ruby: "Ruby", c_cpp: "C/C++", coffee: "CoffeeScript", json: "json", perl: "Perl", clojure: "Clojure", ocaml: "OCaml", csharp: "C#", haxe: "haXe", svg: "SVG", textile: "Textile", groovy: "Groovy", liquid: "Liquid", Scala: "Scala"}, theme: {clouds: "Clouds", clouds_midnight: "Clouds Midnight", cobalt: "Cobalt", crimson_editor: "Crimson Editor", dawn: "Dawn", eclipse: "Eclipse", idle_fingers: "Idle Fingers", kr_theme: "Kr Theme", merbivore: "Merbivore", merbivore_soft: "Merbivore Soft", mono_industrial: "Mono Industrial", monokai: "Monokai", pastel_on_dark: "Pastel On Dark", solarized_dark: "Solarized Dark", solarized_light: "Solarized Light", textmate: "Textmate", twilight: "Twilight", vibrant_ink: "Vibrant Ink"}, gutter: s, fontSize: {"10px": "10px", "11px": "11px", "12px": "12px", "14px": "14px", "16px": "16px"}, softWrap: {off: "Off", 40: "40", 80: "80", free: "Free"}, keybindings: {ace: "ace", vim: "vim", emacs: "emacs"}, showPrintMargin: s, useSoftTabs: s, showInvisibles: s}, a = [];
        a.push("<table><tr><th>Setting</th><th>Value</th></tr>");
        for (var l in i)a.push("<tr><td>", o[l], "</td>"), a.push("<td>"), f(a, l, u[l], i[l]), a.push("</td></tr>");
        a.push("</table>"), e.innerHTML = a.join("");
        var c = function (e) {
            var t = e.currentTarget;
            n.setOption(t.title, t.value)
        }, h = function (e) {
            var t = e.currentTarget;
            n.setOption(t.title, t.checked)
        }, p = e.getElementsByTagName("select");
        for (var d = 0; d < p.length; d++)p[d].onchange = c;
        var v = e.getElementsByTagName("input");
        for (var d = 0; d < v.length; d++)v[d].onclick = h;
        var m = document.createElement("input");
        m.type = "button", m.value = "Hide", r.addListener(m, "click", function () {
            n.setDisplaySettings(!1)
        }), e.appendChild(m), e.hideButton = m
    }

    var r = e("../lib/event"), i = e("../lib/useragent"), s = e("../lib/net"), o = e("../ace");
    e("../theme/textmate"), n.exports = t = o;
    var u = function (e, t, n) {
        var r = e.style[n];
        r || (window.getComputedStyle ? r = window.getComputedStyle(e, "").getPropertyValue(n) : r = e.currentStyle[n]);
        if (!r || r == "auto" || r == "intrinsic")r = t.style[n];
        return r
    };
    t.transformTextarea = function (e, t) {
        var n, s = f(e, function () {
            return n.getValue()
        });
        e.style.display = "none", s.style.background = "white";
        var u = document.createElement("div");
        a(u, {top: "0px", left: "0px", right: "0px", bottom: "0px", border: "1px solid gray", position: "absolute"}), s.appendChild(u);
        var l = document.createElement("div");
        a(l, {position: "absolute", right: "0px", bottom: "0px", background: "red", cursor: "nw-resize", borderStyle: "solid", borderWidth: "9px 8px 10px 9px", width: "2px", borderColor: "lightblue gray gray lightblue", zIndex: 101});
        var p = document.createElement("div"), d = {top: "0px", left: "20%", right: "0px", bottom: "0px", position: "absolute", padding: "5px", zIndex: 100, color: "white", display: "none", overflow: "auto", fontSize: "14px", boxShadow: "-5px 2px 3px gray"};
        i.isOldIE ? d.backgroundColor = "#333" : d.backgroundColor = "rgba(0, 0, 0, 0.6)", a(p, d), s.appendChild(p);
        var v = {}, m = o.edit(u);
        n = m.getSession(), n.setValue(e.value || e.innerHTML), m.focus(), s.appendChild(l), c(m, u, p, o, v, t), h(p, l, m, v);
        var g = "";
        return r.addListener(l, "mousemove", function (e) {
            var t = this.getBoundingClientRect(), n = e.clientX - t.left, r = e.clientY - t.top;
            n + r < (t.width + t.height) / 2 ? (this.style.cursor = "pointer", g = "toggle") : (g = "resize", this.style.cursor = "nw-resize")
        }), r.addListener(l, "mousedown", function (e) {
            if (g == "toggle") {
                m.setDisplaySettings();
                return
            }
            s.style.zIndex = 1e5;
            var t = s.getBoundingClientRect(), n = t.width + t.left - e.clientX, i = t.height + t.top - e.clientY;
            r.capture(l, function (e) {
                s.style.width = e.clientX - t.left + n + "px", s.style.height = e.clientY - t.top + i + "px", m.resize()
            }, function () {
            })
        }), m
    }, t.options = {mode: "text", theme: "textmate", gutter: "false", fontSize: "12px", softWrap: "off", keybindings: "ace", showPrintMargin: "false", useSoftTabs: "true", showInvisibles: "false"}
}), define("ace/theme/textmate", ["require", "exports", "module", "ace/lib/dom"], function (e, t, n) {
    t.isDark = !1, t.cssClass = "ace-tm", t.cssText = '.ace-tm .ace_gutter {background: #f0f0f0;color: #333;}.ace-tm .ace_print-margin {width: 1px;background: #e8e8e8;}.ace-tm .ace_fold {background-color: #6B72E6;}.ace-tm .ace_scroller {background-color: #FFFFFF;}.ace-tm .ace_cursor {border-left: 2px solid black;}.ace-tm .ace_overwrite-cursors .ace_cursor {border-left: 0px;border-bottom: 1px solid black;}.ace-tm .ace_invisible {color: rgb(191, 191, 191);}.ace-tm .ace_storage,.ace-tm .ace_keyword {color: blue;}.ace-tm .ace_constant {color: rgb(197, 6, 11);}.ace-tm .ace_constant.ace_buildin {color: rgb(88, 72, 246);}.ace-tm .ace_constant.ace_language {color: rgb(88, 92, 246);}.ace-tm .ace_constant.ace_library {color: rgb(6, 150, 14);}.ace-tm .ace_invalid {background-color: rgba(255, 0, 0, 0.1);color: red;}.ace-tm .ace_support.ace_function {color: rgb(60, 76, 114);}.ace-tm .ace_support.ace_constant {color: rgb(6, 150, 14);}.ace-tm .ace_support.ace_type,.ace-tm .ace_support.ace_class {color: rgb(109, 121, 222);}.ace-tm .ace_keyword.ace_operator {color: rgb(104, 118, 135);}.ace-tm .ace_string {color: rgb(3, 106, 7);}.ace-tm .ace_comment {color: rgb(76, 136, 107);}.ace-tm .ace_comment.ace_doc {color: rgb(0, 102, 255);}.ace-tm .ace_comment.ace_doc.ace_tag {color: rgb(128, 159, 191);}.ace-tm .ace_constant.ace_numeric {color: rgb(0, 0, 205);}.ace-tm .ace_variable {color: rgb(49, 132, 149);}.ace-tm .ace_xml-pe {color: rgb(104, 104, 91);}.ace-tm .ace_entity.ace_name.ace_function {color: #0000A2;}.ace-tm .ace_markup.ace_heading {color: rgb(12, 7, 255);}.ace-tm .ace_markup.ace_list {color:rgb(185, 6, 144);}.ace-tm .ace_meta.ace_tag {color:rgb(0, 22, 142);}.ace-tm .ace_string.ace_regex {color: rgb(255, 0, 0)}.ace-tm .ace_marker-layer .ace_selection {background: rgb(181, 213, 255);}.ace-tm.ace_multiselect .ace_selection.ace_start {box-shadow: 0 0 3px 0px white;border-radius: 2px;}.ace-tm .ace_marker-layer .ace_step {background: rgb(252, 255, 0);}.ace-tm .ace_marker-layer .ace_stack {background: rgb(164, 229, 101);}.ace-tm .ace_marker-layer .ace_bracket {margin: -1px 0 0 -1px;border: 1px solid rgb(192, 192, 192);}.ace-tm .ace_marker-layer .ace_active-line {background: rgba(0, 0, 0, 0.07);}.ace-tm .ace_gutter-active-line {background-color : #dcdcdc;}.ace-tm .ace_marker-layer .ace_selected-word {background: rgb(250, 250, 255);border: 1px solid rgb(200, 200, 250);}.ace-tm .ace_indent-guide {background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==") right repeat-y;}';
    var r = e("../lib/dom");
    r.importCssString(t.cssText, t.cssClass)
})