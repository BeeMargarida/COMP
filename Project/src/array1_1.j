.class public aval1
.super java/lang/Object

.method public static main([Ljava/lang/String;)V
.limit locals 0
.limit stack 0
iconst_2
iconst_3
invokestatic aval1/f(II)I

istore_0

iload_0
invokestatic aval1/println(I)V

return
.end method


.method public static f(II)I
.limit locals 4
.limit stack 2
iload_0
iload_1
imul
istore_2

iload_2
ireturn
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
