from rest_framework import serializers
from rest_framework.utils import model_meta
from .models import annotation, target, body

class body_serializer(serializers.ModelSerializer):
    class Meta:
        model = body
        fields = '__all__'

class body_return_serializer(serializers.ModelSerializer):
    value = serializers.ReadOnlyField()
    class Meta:
        model = body
        fields=['type','value']

class target_serializer(serializers.ModelSerializer):
    class Meta:
        model = target
        #selector = serializers.ReadOnlyField()
        #fields = ['type', 'IRI', 'selector']
        fields = '__all__'

class target_return_serializer(serializers.ModelSerializer):
    id = serializers.URLField(source='IRI')
    class Meta:
        model = target
        selector = serializers.ReadOnlyField()
        fields = ['id', 'type', 'selector']

class annotation_serializer(serializers.ModelSerializer):
    context = serializers.CharField(required=False, default='http://www.w3.org/ns/anno.jsonld')
    IRI = serializers.URLField(required=False)
    motivation = serializers.CharField(required=False)
    creator = serializers.URLField(required=False)
    target = target_return_serializer(source= 'target_set',read_only=True, many=True)
    body = body_return_serializer(source = 'body_set', read_only=True, many=True)
    id = serializers.URLField(source='IRI',required=False)

    class Meta:
        model = annotation
        fields = ['id', 'context', 'motivation', 'creator', 'motivation', 'IRI', 'target', 'body']

    def create(self, validated_data):
        anno = annotation.objects.create(**validated_data)
        anno.IRI = 'http://www.atlasAnno.org/anno/' + str(anno.id)
        anno.save()
        return anno

