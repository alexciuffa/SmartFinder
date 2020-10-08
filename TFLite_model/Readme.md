# Deploy machine learning models on Android device
O objetivo é integrar um modelo treinado a partir do tensorflow em uma aplicação Android. Para isso, os passos são:

 1. Treinar um modelo simples (`y=2*x-1`)
 2. Converter este modelo em uma versão mobile com o formato TensorFlow Lite
 3. Criar uma aplicação que utilize o modelo

## Virtual Envirolment
O treinamento e conversão do modelo será feito em Python, e para isso este projeto tem um virtualenv com as dependêncaias necessárias. Para iniciar, utilize: `$ source ./venvs/tf_2/bin/activate`

## Bibliografia
https://www.tensorflow.org/lite: possui um passo a passo de como gerar um modelo e otimizar para mobile e deipositivos embarcados (IoT) e exemplos de aplicações prontas
