.class public programa2
.super java/lang/Object

.method public static f1([I)[I
.limit locals 2
.limit stack 4
iconst_0
istore_1


aload_0
arraylength
istore_2


iload_2
newarray int
astore_3

loop0:

iload_1
aload_0
arraylength
if_icmpge loop0_end

aload_3
iload_1
aload_0
iload_1
iaload
iastore
iinc 1 1
goto loop0

loop0_end:


iload_3
ireturn
.end method


.method public static f2(I)[I
.limit locals 3
.limit stack 1
iload_0
newarray int
astore_1

iconst_1
istore_1


iconst_1
istore_2


iload_0
newarray int
astore_3

iload_2
istore_3



iload_1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 3
.limit stack 5
bipush 100
newarray int
astore_1

aload_1
iload_-1
iconst_1
iastore
aload_1
iload_-1
iconst_2
iastore
iload_1
invokestatic programa2/f1(I)I

istore_2


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V

bipush 100
invokestatic programa2/f2(I)I

istore_2


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
