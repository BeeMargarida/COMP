.class public aval6
.super java/lang/Object

.method public static sqrt(I)I
.limit locals 6
.limit stack 3
iload_0
istore_1


iconst_0
istore_2


iconst_0
istore_3


iconst_0
istore 4


iconst_0
istore 5


loop0:

iload 5
bipush 6
if_icmpge loop0_end

iload_2
iload_3
iadd
istore 6


iload 6
iconst_2
ishl
istore 7


iload 7
iconst_1
ior
istore 8


iload_3
iconst_1
ishl
istore 9


iload 4
iconst_2
ishl
istore 10


iload_1
bipush 10
ishr
istore 11


iload 11
iconst_3
iand
istore 12


iload 10
iload 12
ior
istore 4


iload_1
iconst_2
ishl
istore_1


iload 8
iload 4
if_icmpgt loop1_end

iload 9
iconst_1
ior
istore_3


iload 8
istore_2


goto loop1_next
loop1_end:
iload 9
istore_3


iload_2
iconst_2
ishl
istore_2


loop1_next:
iinc 5 1
goto loop0

loop0_end:


iload_3
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 1
.limit stack 1
bipush 17
invokestatic aval6/sqrt(I)I

istore_1


iload_1
invokestatic io/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
