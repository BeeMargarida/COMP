.class public aval4
.super java/lang/Object

.method public static f(II)I
.limit locals 4
.limit stack 2
loop0:

iload_0
iload_1
if_icmpge loop0_end

istore_2


istore_0


iload_0
iload_2
iadd
istore_0


goto loop0

loop0_end:


iload_2
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 1
.limit stack 2
iconst_5
iconst_6
invokestatic aval4/f(II)I

istore_1


iload_1
invokestatic aval4/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
