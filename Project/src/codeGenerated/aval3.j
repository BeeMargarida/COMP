.class public aval3
.super java/lang/Object

.method public static f(II)I
.limit locals 3
.limit stack 0

iload_-1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 2
.limit stack 2
iconst_2
istore_1

iconst_3
istore_2

iload_1
iload_2
invokestatic aval3/f(II)I

istore_1

iload_1
invokestatic aval3/println(I)V

iconst_6
istore_1

iload_1
iload_2
invokestatic aval3/f(II)I

istore_1

iload_1
invokestatic aval3/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
