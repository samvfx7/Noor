package com.samvfx7.noor.data.local

import com.samvfx7.noor.data.model.Ayah
import com.samvfx7.noor.data.model.JuzInfo
import com.samvfx7.noor.data.model.Surah

object QuranRepository {

    val SURAHS_METADATA = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", 7, "Meccan", 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "Medinan", 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, "Medinan", 3),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, "Medinan", 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, "Medinan", 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", 165, "Meccan", 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", 206, "Meccan", 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, "Medinan", 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", 129, "Medinan", 10),
        Surah(10, "يونس", "Yunus", "Jonah", 109, "Meccan", 11),
        Surah(11, "هود", "Hud", "Hud", 123, "Meccan", 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", 111, "Meccan", 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, "Medinan", 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", 52, "Meccan", 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, "Meccan", 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", 128, "Meccan", 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", 111, "Meccan", 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, "Meccan", 15),
        Surah(19, "مريم", "Maryam", "Mary", 98, "Meccan", 16),
        Surah(20, "طه", "Taha", "Ta-Ha", 135, "Meccan", 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", 112, "Meccan", 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, "Medinan", 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, "Meccan", 18),
        Surah(24, "النور", "An-Nur", "The Light", 64, "Medinan", 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", 77, "Meccan", 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, "Meccan", 19),
        Surah(27, "النمل", "An-Naml", "The Ants", 93, "Meccan", 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", 88, "Meccan", 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, "Meccan", 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", 60, "Meccan", 21),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, "Meccan", 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", 30, "Meccan", 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, "Medinan", 21),
        Surah(34, "سبأ", "Saba", "Sheba", 54, "Meccan", 22),
        Surah(35, "فاطر", "Fatir", "The Originator", 45, "Meccan", 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccan", 22),
        Surah(37, "الصافات", "As-Saffat", "Those Ranges in Ranks", 182, "Meccan", 23),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, "Meccan", 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", 75, "Meccan", 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", 85, "Meccan", 24),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", 54, "Meccan", 24),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", 53, "Meccan", 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, "Meccan", 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, "Meccan", 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, "Meccan", 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, "Meccan", 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, "Medinan", 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", 29, "Medinan", 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, "Medinan", 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, "Meccan", 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, "Meccan", 26),
        Surah(52, "الطور", "At-Tur", "The Mount", 49, "Meccan", 27),
        Surah(53, "النجم", "An-Najm", "The Star", 62, "Meccan", 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", 55, "Meccan", 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "Medinan", 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, "Meccan", 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", 29, "Medinan", 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, "Medinan", 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", 24, "Medinan", 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That is Examined", 13, "Medinan", 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", 14, "Medinan", 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", 11, "Medinan", 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, "Medinan", 28),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, "Medinan", 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", 12, "Medinan", 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", 12, "Medinan", 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "Meccan", 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", 52, "Meccan", 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", 52, "Meccan", 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, "Meccan", 29),
        Surah(71, "نوح", "Nuh", "Noah", 28, "Meccan", 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", 28, "Meccan", 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, "Meccan", 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, "Meccan", 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, "Meccan", 29),
        Surah(76, "الإنسان", "Al-Insan", "The Man", 31, "Medinan", 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, "Meccan", 29),
        Surah(78, "النبأ", "An-Naba", "The Tidings", 40, "Meccan", 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", 46, "Meccan", 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", 42, "Meccan", 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", 29, "Meccan", 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, "Meccan", 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, "Meccan", 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", 25, "Meccan", 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, "Meccan", 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", 17, "Meccan", 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", 19, "Meccan", 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, "Meccan", 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", 30, "Meccan", 30),
        Surah(90, "البلد", "Al-Balad", "The City", 20, "Meccan", 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", 15, "Meccan", 30),
        Surah(92, "الليل", "Al-Layl", "The Night", 21, "Meccan", 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", 11, "Meccan", 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", 8, "Meccan", 30),
        Surah(95, "التين", "At-Tin", "The Fig", 8, "Meccan", 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", 19, "Meccan", 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", 5, "Meccan", 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, "Medinan", 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, "Medinan", 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", 11, "Meccan", 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, "Meccan", 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", 8, "Meccan", 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", 3, "Meccan", 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", 9, "Meccan", 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", 5, "Meccan", 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, "Meccan", 30),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindness", 7, "Meccan", 30),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, "Meccan", 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, "Meccan", 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", 3, "Medinan", 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", 5, "Meccan", 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, "Meccan", 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "Meccan", 30),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, "Meccan", 30)
    )

    val JUZ_LIST = listOf(
        JuzInfo(1, "الجزء ١", 1, "Al-Fatihah", 1),
        JuzInfo(2, "الجزء ٢", 2, "Al-Baqarah", 142),
        JuzInfo(3, "الجزء ٣", 2, "Al-Baqarah", 253),
        JuzInfo(4, "الجزء ٤", 3, "Ali 'Imran", 93),
        JuzInfo(5, "الجزء ٥", 4, "An-Nisa", 24),
        JuzInfo(6, "الجزء ٦", 4, "An-Nisa", 148),
        JuzInfo(7, "الجزء ٧", 5, "Al-Ma'idah", 82),
        JuzInfo(8, "الجزء ٨", 6, "Al-An'am", 111),
        JuzInfo(9, "الجزء ٩", 7, "Al-A'raf", 88),
        JuzInfo(10, "الجزء ١٠", 8, "Al-Anfal", 41),
        JuzInfo(11, "الجزء ١١", 9, "At-Tawbah", 93),
        JuzInfo(12, "الجزء ١٢", 11, "Hud", 6),
        JuzInfo(13, "الجزء ١٣", 12, "Yusuf", 53),
        JuzInfo(14, "الجزء ١٤", 15, "Al-Hijr", 1),
        JuzInfo(15, "الجزء ١٥", 17, "Al-Isra", 1),
        JuzInfo(16, "الجزء ١٦", 18, "Al-Kahf", 75),
        JuzInfo(17, "الجزء ١٧", 21, "Al-Anbiya", 1),
        JuzInfo(18, "الجزء ١٨", 23, "Al-Mu'minun", 1),
        JuzInfo(19, "الجزء ١٩", 25, "Al-Furqan", 21),
        JuzInfo(20, "الجزء ٢٠", 27, "An-Naml", 56),
        JuzInfo(21, "الجزء ٢١", 29, "Al-'Ankabut", 46),
        JuzInfo(22, "الجزء ٢٢", 33, "Al-Ahzab", 31),
        JuzInfo(23, "الجزء ٢٣", 36, "Ya-Sin", 28),
        JuzInfo(24, "الجزء ٢٤", 39, "Az-Zumar", 32),
        JuzInfo(25, "الجزء ٢٥", 41, "Fussilat", 47),
        JuzInfo(26, "الجزء ٢٦", 46, "Al-Ahqaf", 1),
        JuzInfo(27, "الجزء ٢٧", 51, "Adh-Dhariyat", 31),
        JuzInfo(28, "الجزء ٢٨", 58, "Al-Mujadila", 1),
        JuzInfo(29, "الجزء ٢٩", 67, "Al-Mulk", 1),
        JuzInfo(30, "الجزء ٣٠", 78, "An-Naba", 1)
    )

    // Detailed Ayahs for prominent Surahs
    private val AUTHENTIC_SURAHS_AYAHS = mapOf(
        1 to listOf(
            Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Bismillaahir-Rahmaanir-Raheem", 1),
            Ayah(2, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "Alhamdu lillaahi Rabbil-'aalameen", 1),
            Ayah(3, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Ar-Rahmaanir-Raheem", 1),
            Ayah(4, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Maaliki Yawmid-Deen", 1),
            Ayah(5, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "Iyyaaka na'budu wa lyyaaka nasta'een", 1),
            Ayah(6, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Ihdinas-Siraatal-Mustaqeem", 1),
            Ayah(7, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "Siraatal-lazeena an'amta 'alayhim ghayril-maghdoobi 'alayhim wa lad-daalleen", 1)
        ),
        112 to listOf(
            Ayah(1, 6222, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "Qul Huwal-laahu Ahad", 30),
            Ayah(2, 6223, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "Allahus-Samad", 30),
            Ayah(3, 6224, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "Lam yalid wa lam yoolad", 30),
            Ayah(4, 6225, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "Wa lam yakul-lahu kufuwan ahad", 30)
        ),
        113 to listOf(
            Ayah(1, 6226, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "Qul a'oozu bi rabbil-falaq", 30),
            Ayah(2, 6227, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "Min sharri maa khalaq", 30),
            Ayah(3, 6228, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "Wa min sharri ghaasiqin izaa waqab", 30),
            Ayah(4, 6229, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "Wa min sharrin-naffaasaati fil 'uqad", 30),
            Ayah(5, 6230, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "Wa min sharri haasidin izaa hasad", 30)
        ),
        114 to listOf(
            Ayah(1, 6231, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "Qul a'oozu bi rabbin-naas", 30),
            Ayah(2, 6232, "مَلِكِ النَّاسِ", "The Sovereign of mankind,", "Malikin-naas", 30),
            Ayah(3, 6233, "إِلَٰهِ النَّاسِ", "The God of mankind,", "Ilaahin-naas", 30),
            Ayah(4, 6234, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "Min sharril-waswaasil-khannaas", 30),
            Ayah(5, 6235, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers into the breasts of mankind -", "Allazee yuwaswisu fee sudoorin-naas", 30),
            Ayah(6, 6236, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "Minal-jinnati wan-naas", 30)
        ),
        108 to listOf(
            Ayah(1, 6205, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "Innaaa a'tainaakal-Kawthar", 30),
            Ayah(2, 6206, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", "Fasalli li Rabbika wanhar", 30),
            Ayah(3, 6207, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.", "Inna shaani'aka huwal abtar", 30)
        ),
        103 to listOf(
            Ayah(1, 6177, "وَالْعَصْرِ", "By time,", "Wal-'Asr", 30),
            Ayah(2, 6178, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "Innal-insaana lafee khusr", 30),
            Ayah(3, 6179, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "Illal-lazeena aamanoo wa 'amilus-saalihaati wa tawaasaw bil-haqqi wa tawaasaw bis-sabr", 30)
        ),
        67 to listOf(
            Ayah(1, 5242, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", "Tabaarakal-lazee biyadihil-mulku wa Huwa 'alaa kulli shai'in Qadeer", 29),
            Ayah(2, 5243, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -", "Allazee khalaqal mawta walhayaata liyabluwakum ayyukum ahsanu 'amalaa; wa Huwal 'Azeezul Ghafoor", 29),
            Ayah(3, 5244, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency.", "Allazee khalaqa sab'a samaawaatin tibaaqam maa taraa fee khalqir Rahmaani min tafaawut", 29)
        ),
        36 to listOf(
            Ayah(1, 3706, "يس", "Ya, Seen.", "Yaa-Seeen", 22),
            Ayah(2, 3707, "وَالْقُرْآنِ الْحَكِيمِ", "By the wise Qur'an.", "Wal-Qur-aanil-Hakeem", 22),
            Ayah(3, 3708, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Indeed you, [O Muhammad], are from among the messengers,", "Innaka laminal mursaleen", 22),
            Ayah(4, 3709, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "On a straight path.", "'Alaa Siraatim Mustaqeem", 22),
            Ayah(5, 3710, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "[This is] a revelation of the Exalted in Might, the Merciful,", "Tanzeelal 'Azeezir Raheem", 22)
        ),
        55 to listOf(
            Ayah(1, 4902, "الرَّحْمَٰنُ", "The Most Merciful", "Ar-Rahmaan", 27),
            Ayah(2, 4903, "عَلَّمَ الْقُرْآنَ", "Taught the Qur'an,", "'Allamal-Qur'aan", 27),
            Ayah(3, 4904, "خَلَقَ الْإِنسَانَ", "Created man,", "Khalaqal-insaan", 27),
            Ayah(4, 4905, "عَلَّمَهُ الْبَيَانَ", "Taught him eloquent speech.", "'Allamahul-bayaan", 27),
            Ayah(5, 4906, "الشَّمْسُ وَالْقَمَرُ بِحُسْبَانٍ", "The sun and the moon [move] by precise calculation,", "Ash-shamsu wal-qamaru bihusbaan", 27)
        )
    )

    fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        val surah = SURAHS_METADATA.find { it.number == surahNumber } ?: return emptyList()
        val predefined = AUTHENTIC_SURAHS_AYAHS[surahNumber]
        if (predefined != null) {
            return predefined
        }

        // For other surahs, generate structured ayahs with Bismillah and authentic placeholders
        return (1..surah.numberOfAyahs).map { ayahNum ->
            Ayah(
                numberInSurah = ayahNum,
                numberInQuran = surahNumber * 100 + ayahNum,
                arabicText = if (ayahNum == 1) "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ • آية $ayahNum" else "آية $ayahNum من سورة ${surah.nameArabic}",
                translationEnglish = "Verse $ayahNum of Surah ${surah.nameEnglish} (${surah.nameTranslation}).",
                transliteration = "Ayah $ayahNum fee Surah ${surah.nameEnglish}",
                juz = surah.startJuz
            )
        }
    }

    fun searchSurahs(query: String): List<Surah> {
        if (query.isBlank()) return SURAHS_METADATA
        val q = query.trim().lowercase()
        return SURAHS_METADATA.filter {
            it.nameEnglish.lowercase().contains(q) ||
            it.nameTranslation.lowercase().contains(q) ||
            it.nameArabic.contains(q) ||
            it.number.toString() == q
        }
    }
}
