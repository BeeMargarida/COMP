.class public aval8
.super java/lang/Object

.method public static max1()I
.limit locals 5
.limit stack 4
invokestatic io/read()I

istore_0


invokestatic io/read()I

istore_1


iload_1
istore_2


iload_0
iload_1
if_icmple loop0_end

iload_0
istore_2


loop0_end:
iconst_2
iconst_4
imul
istore_3


ldc "a"
iload_0
invokestatic io/print(Ljava/lang/String;I)V

iload_0
bipush -23
if_icmpge loop1_end

iconst_0
istore 4


goto loop1_next
loop1_end:
bipush -2
iconst_4
imul
istore 4


loop1_next:
iload 4
istore_2



iload_2
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 2
.limit stack 1
invokestatic aval8/max1()I

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
