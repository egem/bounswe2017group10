# -*- coding: utf-8 -*-
# Generated by Django 1.11.5 on 2017-12-03 13:58
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('atlas', '0013_item_visit'),
    ]

    operations = [
        migrations.CreateModel(
            name='hidden_tag',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=100)),
            ],
        ),
        migrations.AddField(
            model_name='cultural_heritage',
            name='hidden_tags',
            field=models.ManyToManyField(blank=True, to='atlas.hidden_tag'),
        ),
    ]