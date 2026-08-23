from pathlib import Path
import re

path = Path(__file__).resolve().parents[1] / "app/src/main/res/values-ar/strings_ui.xml"
text = path.read_text()

forms = {
    "budget_days_left": {
        "zero": "لا أيام متبقية", "one": "%d يوم متبقٍ", "two": "%d يومان متبقيان",
        "few": "%d أيام متبقية", "many": "%d يومًا متبقيًا", "other": "%d يوم متبقٍ",
    },
    "meditation_best_streak": {
        "zero": "لا توجد سلسلة", "one": "أفضل سلسلة: %d يوم", "two": "أفضل سلسلة: يومان",
        "few": "أفضل سلسلة: %d أيام", "many": "أفضل سلسلة: %d يومًا", "other": "أفضل سلسلة: %d يوم",
    },
    "meditation_minutes": {
        "zero": "0 دقيقة", "one": "%d دقيقة", "two": "%d دقيقتان",
        "few": "%d دقائق", "many": "%d دقيقة", "other": "%d دقيقة",
    },
    "meditation_streak": {
        "zero": "لا أيام على التوالي", "one": "%d يوم على التوالي", "two": "%d يومان على التوالي",
        "few": "%d أيام على التوالي", "many": "%d يومًا على التوالي", "other": "%d يوم على التوالي",
    },
    "meditation_week_entries": {
        "zero": "لا ملاحظات", "one": "%d ملاحظة", "two": "%d ملاحظتان",
        "few": "%d ملاحظات", "many": "%d ملاحظة", "other": "%d ملاحظة",
    },
    "notes_count": {
        "zero": "لا ملاحظات", "one": "%d ملاحظة", "two": "%d ملاحظتان",
        "few": "%d ملاحظات", "many": "%d ملاحظة", "other": "%d ملاحظة",
    },
    "notes_count_list": {
        "zero": "لا قوائم", "one": "%d قائمة", "two": "%d قائمتان",
        "few": "%d قوائم", "many": "%d قائمة", "other": "%d قائمة",
    },
    "notes_count_note": {
        "zero": "لا ملاحظات", "one": "%d ملاحظة", "two": "%d ملاحظتان",
        "few": "%d ملاحظات", "many": "%d ملاحظة", "other": "%d ملاحظة",
    },
    "notes_count_routine": {
        "zero": "لا روتينات", "one": "%d روتين", "two": "%d روتينان",
        "few": "%d روتينات", "many": "%d روتينًا", "other": "%d روتين",
    },
    "notes_selected": {
        "zero": "لا عناصر محددة", "one": "%d عنصر محدد", "two": "%d عنصران محددان",
        "few": "%d عناصر محددة", "many": "%d عنصرًا محددًا", "other": "%d عنصر محدد",
    },
    "reminder_in_hours": {
        "zero": "بعد %1$d ساعة · %2$s", "one": "بعد %1$d ساعة · %2$s", "two": "بعد ساعتين · %2$s",
        "few": "بعد %1$d ساعات · %2$s", "many": "بعد %1$d ساعة · %2$s", "other": "بعد %1$d ساعة · %2$s",
    },
    "reminder_in_minutes": {
        "zero": "بعد %1$d دقيقة · %2$s", "one": "بعد %1$d دقيقة · %2$s", "two": "بعد دقيقتين · %2$s",
        "few": "بعد %1$d دقائق · %2$s", "many": "بعد %1$d دقيقة · %2$s", "other": "بعد %1$d دقيقة · %2$s",
    },
    "tag_delete_with_notes_text": {
        "zero": "احذف “#%1$s” من دون ملاحظات. لا يمكن التراجع عن ذلك.",
        "one": "احذف “#%1$s” والملاحظة %2$d التي تحملها. لا يمكن التراجع عن ذلك.",
        "two": "احذف “#%1$s” والملاحظتين %2$d اللتين تحملانهما. لا يمكن التراجع عن ذلك.",
        "few": "احذف “#%1$s” والملاحظات الـ%2$d التي تحملها. لا يمكن التراجع عن ذلك.",
        "many": "احذف “#%1$s” والملاحظة الـ%2$d التي تحملها. لا يمكن التراجع عن ذلك.",
        "other": "احذف “#%1$s” وجميع الملاحظات الـ%2$d التي تحملها. لا يمكن التراجع عن ذلك.",
    },
    "trash_delete_confirm_text": {
        "zero": "لا توجد ملاحظات ستحذف نهائيًا. لا يمكن التراجع عن هذا.",
        "one": "%d ملاحظة ستحذف نهائيًا. لا يمكن التراجع عن هذا.",
        "two": "%d ملاحظتان ستحذفان نهائيًا. لا يمكن التراجع عن هذا.",
        "few": "%d ملاحظات ستحذف نهائيًا. لا يمكن التراجع عن هذا.",
        "many": "%d ملاحظة ستحذف نهائيًا. لا يمكن التراجع عن هذا.",
        "other": "%d ملاحظات ستحذف نهائيًا. لا يمكن التراجع عن هذا.",
    },
    "trash_restore_confirm_text": {
        "zero": "لا توجد ملاحظات تعود إلى ملاحظاتك.",
        "one": "%d ملاحظة تعود إلى ملاحظاتك.",
        "two": "%d ملاحظتان تعودان إلى ملاحظاتك.",
        "few": "%d ملاحظات تعود إلى ملاحظاتك.",
        "many": "%d ملاحظة تعود إلى ملاحظاتك.",
        "other": "%d ملاحظات تعود إلى ملاحظاتك.",
    },
    "trash_tag_text": {
        "zero": "لا توجد ملاحظات في سلة المهملات تحمل هذا الوسم.",
        "one": "%d ملاحظة في سلة المهملات تحمل هذا الوسم.",
        "two": "%d ملاحظتان في سلة المهملات تحملان هذا الوسم.",
        "few": "%d ملاحظات في سلة المهملات تحمل هذا الوسم.",
        "many": "%d ملاحظة في سلة المهملات تحمل هذا الوسم.",
        "other": "%d ملاحظات في سلة المهملات تحمل هذا الوسم.",
    },
}

for name, values in forms.items():
    block = "\n".join([f'    <plurals name="{name}">'] + [
        f'        <item quantity="{quantity}">{value}</item>' for quantity, value in values.items()
    ] + ["    </plurals>"])
    pattern = rf'    <plurals name="{re.escape(name)}">.*?    </plurals>'
    text, count = re.subn(pattern, block, text, count=1, flags=re.S)
    if count != 1:
        raise SystemExit(f"plural not found exactly once: {name}")

path.write_text(text)
print(f"updated {len(forms)} Arabic plural resources")
