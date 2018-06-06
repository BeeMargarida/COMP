.class public semantic
.super java/lang/Object

.method public static f1(II)[I
.limit locals 3
.limit stack 0
invokestatic null/f4()I


.end method


.method public static f2(III)V
.limit locals 6
.limit stack 2
invokestatic io/print()V

iconst_1
istore_0


iload_1
iload_2
invokestatic semantic/f1(II)[I

astore_0

bipush 100
newarray int
astore_3

iconst_1
istore 4


iload_3
istore 4


bipush 100
newarray int
astore_5

aload 5
invokestatic semantic/f3([I)V


return
.end method


.method public static f3(I)V
.limit locals 4
.limit stack 3
iload_0
iconst_0
if_icmple loop0_end

iconst_1
istore_1


goto loop0_next
loop0_end:
bipush 100
newarray int
astore_1

loop0_next:
iconst_1
istore_2


iload_2
istore_1



return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
