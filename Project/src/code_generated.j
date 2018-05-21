.class public aval1
.super java/lang/Object

.method public static main([Ljava/lang/String;)V
<<<<<<< HEAD
.limit locals 1
iconst_2
iconst_3
invokestatic aval1/f(II)I

istore_1

iconst_2
iconst_3
invokestatic aval1/b(II)V

iload_1
invokestatic aval1/println(I)V

=======
.limit locals 2
null
istore_1

>>>>>>> 23a2f2d38f18b2ed97ba2544ef9bf73a41c9643b

return
.end method


<<<<<<< HEAD
.method public static f(II)I
.limit locals 3
iload_0
iload_1
imul
istore_2

null
istore_2


iload_2
ireturn
.end method


.method public static b(II)V
.limit locals 2
=======
.method public static f([I)I
.limit locals 1
>>>>>>> 23a2f2d38f18b2ed97ba2544ef9bf73a41c9643b

return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
